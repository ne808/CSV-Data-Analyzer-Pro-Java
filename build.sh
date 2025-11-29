#!/bin/bash

# Data Analyzer Pro - Build and Run Script
# ==========================================

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_DIR/src"
BUILD_DIR="$PROJECT_DIR/build"
MAIN_CLASS="analyzer.Main"

# Terminal colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "╔════════════════════════════════════════════════════════════╗"
echo "║           DATA ANALYZER PRO - Build System                 ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Function to display usage
show_usage() {
    echo -e "${YELLOW}Usage:${NC} $0 [command]"
    echo ""
    echo "Commands:"
    echo "  build    - Compile the project"
    echo "  run      - Run the application"
    echo "  all      - Build and run (default)"
    echo "  clean    - Remove build directory"
    echo "  jar      - Create executable JAR file"
    echo "  help     - Show this help message"
    echo ""
}

# Function to check Java installation
check_java() {
    if ! command -v java &> /dev/null; then
        echo -e "${RED}Error: Java is not installed or not in PATH${NC}"
        exit 1
    fi
    
    if ! command -v javac &> /dev/null; then
        echo -e "${RED}Error: Java compiler (javac) is not installed${NC}"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo -e "${GREEN}✓ Java found:${NC} $JAVA_VERSION"
}

# Function to clean build directory
clean() {
    echo -e "${YELLOW}Cleaning build directory...${NC}"
    rm -rf "$BUILD_DIR"
    echo -e "${GREEN}✓ Clean complete${NC}"
}

# Function to compile the project
build() {
    echo -e "${YELLOW}Compiling project...${NC}"
    
    # Create build directory
    mkdir -p "$BUILD_DIR"
    
    # Find all Java source files
    SOURCES=$(find "$SRC_DIR" -name "*.java")
    SOURCE_COUNT=$(echo "$SOURCES" | wc -l)
    
    echo -e "  Found ${BLUE}$SOURCE_COUNT${NC} source files"
    
    # Compile all sources
    javac -d "$BUILD_DIR" -sourcepath "$SRC_DIR" $SOURCES 2>&1
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Compilation successful${NC}"
        
        # Count compiled classes
        CLASS_COUNT=$(find "$BUILD_DIR" -name "*.class" | wc -l)
        echo -e "  Generated ${BLUE}$CLASS_COUNT${NC} class files"
    else
        echo -e "${RED}✗ Compilation failed${NC}"
        exit 1
    fi
}

# Function to run the application
run() {
    if [ ! -d "$BUILD_DIR" ] || [ -z "$(ls -A $BUILD_DIR 2>/dev/null)" ]; then
        echo -e "${YELLOW}Build directory not found. Building first...${NC}"
        build
    fi
    
    echo -e "${YELLOW}Launching Data Analyzer Pro...${NC}"
    echo ""
    
    cd "$BUILD_DIR"
    java $MAIN_CLASS
}

# Function to create JAR file
create_jar() {
    if [ ! -d "$BUILD_DIR" ] || [ -z "$(ls -A $BUILD_DIR 2>/dev/null)" ]; then
        echo -e "${YELLOW}Build directory not found. Building first...${NC}"
        build
    fi
    
    echo -e "${YELLOW}Creating JAR file...${NC}"
    
    JAR_FILE="$PROJECT_DIR/DataAnalyzerPro.jar"
    
    # Create manifest
    MANIFEST_FILE="$BUILD_DIR/MANIFEST.MF"
    echo "Manifest-Version: 1.0" > "$MANIFEST_FILE"
    echo "Main-Class: $MAIN_CLASS" >> "$MANIFEST_FILE"
    echo "" >> "$MANIFEST_FILE"
    
    # Create JAR
    cd "$BUILD_DIR"
    jar cfm "$JAR_FILE" "$MANIFEST_FILE" analyzer/*.class
    
    if [ $? -eq 0 ]; then
        JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
        echo -e "${GREEN}✓ JAR created:${NC} $JAR_FILE (${JAR_SIZE})"
        echo ""
        echo -e "Run with: ${BLUE}java -jar DataAnalyzerPro.jar${NC}"
    else
        echo -e "${RED}✗ JAR creation failed${NC}"
        exit 1
    fi
}

# Main script logic
check_java

COMMAND=${1:-all}

case $COMMAND in
    build)
        build
        ;;
    run)
        run
        ;;
    all)
        build
        run
        ;;
    clean)
        clean
        ;;
    jar)
        create_jar
        ;;
    help|--help|-h)
        show_usage
        ;;
    *)
        echo -e "${RED}Unknown command: $COMMAND${NC}"
        show_usage
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}Done!${NC}"
