#!/bin/bash

# load-test.sh - Script to demonstrate blocking vs subscribeOn vs reactive approaches

echo "🚀 WebClient in Spring MVC: .block() vs subscribeOn() vs Reactive Demo"
echo "======================================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check if service is running
check_service() {
    local url=$1
    local name=$2
    if curl -s -f "$url" > /dev/null; then
        echo -e "${GREEN}✅ $name is running${NC}"
        return 0
    else
        echo -e "${RED}❌ $name is not running${NC}"
        return 1
    fi
}

echo -e "\n${YELLOW}Checking services...${NC}"
check_service "http://localhost:8080/api/health" "Mock Service (port 8080)" || exit 1
check_service "http://localhost:8081/actuator/health" "Spring MVC Client (port 8081)" || exit 1
check_service "http://localhost:8082/actuator/health" "Non-Blocking Client (port 8082)" || exit 1

echo -e "\n${YELLOW}Starting comprehensive load test...${NC}"

# Test 1: Single request comparison
echo -e "\n${YELLOW}Test 1: Single Request Performance Comparison${NC}"
echo "============================================"

echo -e "${RED}Testing .block() approach (BAD):${NC}"
time curl -s "http://localhost:8081/mvc/blocking/test1" | jq '.totalTime, .warning'

echo -e "\n${BLUE}Testing subscribeOn() approach (BETTER):${NC}"
time curl -s "http://localhost:8081/mvc/subscribe-on/test1" | jq '.totalTime, .warning'

echo -e "\n${GREEN}Testing reactive Mono return (BEST):${NC}"
time curl -s "http://localhost:8081/mvc/reactive/test1" | jq '.totalTime, .warning'

echo -e "\n${GREEN}Testing coroutines approach:${NC}"
time curl -s "http://localhost:8082/non-blocking/single/test1" | jq '.totalTime, .message'

# Test 2: Multiple requests
echo -e "\n${YELLOW}Test 2: Multiple Requests (Shows Concurrency Difference)${NC}"
echo "======================================================="

echo -e "${RED}Testing .block() multiple (sequential - SLOW):${NC}"
time curl -s "http://localhost:8081/mvc/blocking-multiple?ids=item1,item2,item3" | jq '.totalTime, .warning'

echo -e "\n${BLUE}Testing subscribeOn() multiple (concurrent - FAST):${NC}"
time curl -s "http://localhost:8081/mvc/subscribe-on-multiple?ids=item1,item2,item3" | jq '.totalTime, .warning'

echo -e "\n${GREEN}Testing coroutines multiple (concurrent - FAST):${NC}"
time curl -s "http://localhost:8082/non-blocking/multiple?ids=item1,item2,item3" | jq '.totalTime, .message'

# Test 3: Concurrent load test to show thread blocking
echo -e "\n${YELLOW}Test 3: Concurrent Load Test - Thread Pool Exhaustion${NC}"
echo "====================================================="

echo -e "${RED}Sending 5 concurrent .block() requests...${NC}"
echo "⚠️  Watch console logs - servlet threads will be blocked!"

# Create a temporary directory for results
mkdir -p /tmp/load-test-results

# Function to make concurrent requests with timing
make_concurrent_requests() {
    local url=$1
    local name=$2
    local color=$3
    local results_file="/tmp/load-test-results/${name}.txt"

    echo -e "${color}Starting 5 concurrent requests to $name...${NC}"

    start_time=$(date +%s.%N)
    for i in {1..5}; do
        (
            request_start=$(date +%s.%N)
            curl -s "$url" > /dev/null 2>&1
            request_end=$(date +%s.%N)
            duration=$(echo "$request_end - $request_start" | bc -l 2>/dev/null || echo "N/A")
            echo "Request $i: ${duration}s" >> "$results_file"
        ) &
    done

    wait
    end_time=$(date +%s.%N)
    total_time=$(echo "$end_time - $start_time" | bc -l 2>/dev/null || echo "N/A")

    echo -e "${color}Results for $name (Total time: ${total_time}s):${NC}"
    if [ -f "$results_file" ]; then
        cat "$results_file"
        rm "$results_file"
    fi
    echo ""
}

# Test different approaches
make_concurrent_requests "http://localhost:8081/mvc/blocking-multiple?ids=load1,load2,load3" "blocking (.block)" "$RED"

make_concurrent_requests "http://localhost:8081/mvc/subscribe-on-multiple?ids=load1,load2,load3" "subscribeOn" "$BLUE"

make_concurrent_requests "http://localhost:8082/non-blocking/multiple?ids=load1,load2,load3" "coroutines" "$GREEN"

# Test 4: Thread analysis
echo -e "\n${YELLOW}Test 4: Thread Pool Analysis${NC}"
echo "============================"

echo -e "${RED}Spring MVC client thread analysis:${NC}"
curl -s "http://localhost:8081/actuator/threaddump" | \
    jq -r '.threads[] | select(.threadName | contains("http-nio")) | "\(.threadName): \(.threadState)"' | \
    head -5

echo -e "\n${GREEN}Non-blocking client thread analysis:${NC}"
curl -s "http://localhost:8082/actuator/threaddump" | \
    jq -r '.threads[] | select(.threadName | contains("http-nio")) | "\(.threadName): \(.threadState)"' | \
    head -5

# Test 5: Performance metrics
echo -e "\n${YELLOW}Test 5: HTTP Metrics Comparison${NC}"
echo "==============================="

echo -e "${RED}Spring MVC client metrics:${NC}"
curl -s "http://localhost:8081/actuator/metrics/http.server.requests" | \
    jq '.measurements[] | select(.statistic == "TOTAL_TIME") | .value' | \
    head -3

echo -e "\n${GREEN}Non-blocking client metrics:${NC}"
curl -s "http://localhost:8082/actuator/metrics/http.server.requests" | \
    jq '.measurements[] | select(.statistic == "TOTAL_TIME") | .value' | \
    head -3

# Cleanup
rmdir /tmp/load-test-results 2>/dev/null

echo -e "\n${YELLOW}Summary & Best Practices:${NC}"
echo "========================="
echo -e "${RED}❌ NEVER DO: .block() in Spring MVC${NC}"
echo "   ❌ Blocks precious servlet threads"
echo "   ❌ Sequential execution (slow)"
echo "   ❌ Thread pool exhaustion under load"
echo "   ❌ Poor scalability"
echo ""
echo -e "${BLUE}✅ BETTER: subscribeOn() with proper scheduler${NC}"
echo "   ✅ Offloads I/O to separate thread pool"
echo "   ✅ Servlet threads stay free"
echo "   ✅ Can handle concurrent execution"
echo "   ✅ Return CompletableFuture from controllers"
echo ""
echo -e "${GREEN}✅ BEST: Return Mono/Flux directly or use coroutines${NC}"
echo "   ✅ Let Spring MVC handle reactively"
echo "   ✅ Most efficient resource usage"
echo "   ✅ Non-blocking all the way"
echo "   ✅ True reactive streams"

echo -e "\n${YELLOW}Key Rules for Spring MVC + WebClient:${NC}"
echo "1. NEVER use .block() - it kills performance"
echo "2. Use .subscribeOn(Schedulers.boundedElastic()) for I/O"
echo "3. Return CompletableFuture<T> or Mono<T> from controllers"
echo "4. Consider WebFlux if you need full reactive stack"
echo "5. Use coroutines in Kotlin for cleaner async code"

echo -e "\n${BLUE}Demo URLs to try manually:${NC}"
echo "Bad:    http://localhost:8081/mvc/blocking/test123"
echo "Better: http://localhost:8081/mvc/subscribe-on/test123"
echo "Best:   http://localhost:8081/mvc/reactive/test123"
echo "        http://localhost:8082/non-blocking/single/test123" 1

echo -e "\n${YELLOW}Starting load test...${NC}"

# Test 1: Single request comparison
echo -e "\n${YELLOW}Test 1: Single Request Performance${NC}"
echo "-----------------------------------"

echo -e "${RED}Testing BLOCKING client:${NC}"
time curl -s "http://localhost:8081/blocking/single/test1" | jq '.totalTime, .warning'

echo -e "\n${GREEN}Testing NON-BLOCKING client:${NC}"
time curl -s "http://localhost:8082/non-blocking/single/test1" | jq '.totalTime, .message'

# Test 2: Multiple sequential requests
echo -e "\n${YELLOW}Test 2: Multiple Sequential Requests${NC}"
echo "------------------------------------"

echo -e "${RED}Testing BLOCKING client (3 items):${NC}"
time curl -s "http://localhost:8081/blocking/multiple?ids=item1,item2,item3" | jq '.totalTime, .warning'

echo -e "\n${GREEN}Testing NON-BLOCKING client (3 items):${NC}"
time curl -s "http://localhost:8082/non-blocking/multiple?ids=item1,item2,item3" | jq '.totalTime, .message'

# Test 3: Concurrent load test
echo -e "\n${YELLOW}Test 3: Concurrent Load Test (This will show the real difference!)${NC}"
echo "----------------------------------------------------------------"

echo -e "${RED}Sending 5 concurrent requests to BLOCKING client...${NC}"
echo "Watch the console logs - you'll see threads getting blocked!"

# Create a temporary directory for results
mkdir -p /tmp/load-test-results

# Function to make concurrent requests
make_concurrent_requests() {
    local url=$1
    local name=$2
    local results_file="/tmp/load-test-results/${name}.txt"

    echo "Starting concurrent requests to $name..."

    for i in {1..5}; do
        (
            start_time=$(date +%s.%N)
            curl -s "$url" > /dev/null
            end_time=$(date +%s.%N)
            duration=$(echo "$end_time - $start_time" | bc)
            echo "Request $i completed in ${duration}s" >> "$results_file"
        ) &
    done

    wait

    echo "Results for $name:"
    cat "$results_file"
    rm "$results_file"
}

# Test blocking client
make_concurrent_requests "http://localhost:8081/blocking/multiple?ids=load1,load2,load3" "blocking"

echo -e "\n${GREEN}Now sending 5 concurrent requests to NON-BLOCKING client...${NC}"
echo "You should see much better performance!"

# Test non-blocking client
make_concurrent_requests "http://localhost:8082/non-blocking/multiple?ids=load1,load2,load3" "non-blocking"

# Thread dump analysis
echo -e "\n${YELLOW}Test 4: Thread Analysis${NC}"
echo "----------------------"

echo -e "${RED}BLOCKING client thread dump (look for blocked threads):${NC}"
curl -s "http://localhost:8081/actuator/threaddump" | jq '.threads[] | select(.threadState == "TIMED_WAITING" or .threadState == "BLOCKED") | {threadName, threadState}' | head -10

echo -e "\n${GREEN}NON-BLOCKING client thread dump:${NC}"
curl -s "http://localhost:8082/actuator/threaddump" | jq '.threads[] | select(.threadState == "TIMED_WAITING" or .threadState == "BLOCKED") | {threadName, threadState}' | head -10

# Cleanup
rmdir /tmp/load-test-results 2>/dev/null

echo -e "\n${YELLOW}Summary:${NC}"
echo "========"
echo -e "${RED}❌ Blocking Client Issues:${NC}"
echo "   - Blocks servlet threads"
echo "   - Sequential execution (slow)"
echo "   - Limited by thread pool size"
echo "   - Poor resource utilization"
echo ""
echo -e "${GREEN}✅ Non-Blocking Client Benefits:${NC}"
echo "   - Never blocks threads"
echo "   - Concurrent execution (fast)"
echo "   - Better scalability"
echo "   - Efficient resource usage"

echo -e "\n${YELLOW}Key Takeaway:${NC}"
echo "Never use .block() with WebClient in Spring MVC!"
echo "Use suspend functions or return Mono/Flux instead."