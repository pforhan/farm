#!/bin/bash

# Script to extract the Farm Project files from a single text file
# Usage: ./extract_farm_project.sh <input_file_path>
# Example: ./extract_farm_project.sh "Farm Project - All File Contents.txt"

INPUT_FILE_RAW="$1" # Store the raw input path
PROJECT_ROOT="farm" # The top-level directory for the extracted project

# --- Pre-check and Setup ---
if [ -z "$INPUT_FILE_RAW" ]; then
  echo "Usage: ./extract_farm_project.sh <input_file_path>"
  echo "Please provide the path to the text file containing the project content."
  exit 1
fi

# Resolve the absolute path of the input file BEFORE changing directories
# This makes file reading reliable regardless of subsequent `cd` commands.
INPUT_FILE=$(realpath "$INPUT_FILE_RAW")

if [ ! -f "$INPUT_FILE" ]; then
  echo "Error: Input file '$INPUT_FILE' not found or is not a regular file."
  exit 1
fi

echo "Starting extraction of Farm Project from '$INPUT_FILE'..."

# Create the main project root directory if it doesn't exist
mkdir -p "$PROJECT_ROOT"
# Change to the project root directory so all subsequent paths are relative to it
cd "$PROJECT_ROOT" || { echo "Error: Could not enter directory $PROJECT_ROOT"; exit 1; }
echo "Changed directory to: $PWD" # Debugging: confirm current directory

# Variables to track current file state
CURRENT_FILE_PATH=""
COLLECTING_CONTENT=false
FILE_CONTENT=""

# Function to write content to the current file and reset state
write_current_file() {
  echo "DEBUG: Attempting to write file." # Debugging
  if [ -n "$CURRENT_FILE_PATH" ] && [ -n "$FILE_CONTENT" ]; then
    mkdir -p "$(dirname "$CURRENT_FILE_PATH")" # Ensure directory exists
    # Use printf to avoid issues with echo -e and ensure no extra newline at EOF
    printf "%s" "${FILE_CONTENT%?}" > "$CURRENT_FILE_PATH" # Trim trailing newline added by loop
    echo "  Created: $CURRENT_FILE_PATH"
  else
    echo "DEBUG: No file to write or content is empty. Path: '$CURRENT_FILE_PATH', Content length: ${#FILE_CONTENT}" # Debugging
  fi
  # Reset state for the next file
  CURRENT_FILE_PATH=""
  FILE_CONTENT=""
  COLLECTING_CONTENT=false
}

echo "DEBUG: Entering file reading loop." # Debugging
# Read the input file line by line using its absolute path
while IFS= read -r line || [[ -n "$line" ]]; do # Ensures last line is read even if no newline
  # Echo each line read for debugging
  # echo "DEBUG: Read line: '$line'"

  # Detect file path markers (e.g., #### `farm/config/config.php.dist`)
  if [[ "$line" =~ ^####\\s+\`(farm/.*)\`$ ]]; then
    echo "DEBUG: Detected file path marker: '$line'" # Debugging
    write_current_file # Write content of previous file if any
    CURRENT_FILE_PATH="${BASH_REMATCH[1]}"
    # Remove the 'farm/' prefix as we are already inside the 'farm' directory
    CURRENT_FILE_PATH="${CURRENT_FILE_PATH#farm/}"
    echo "DEBUG: New file path set to: '$CURRENT_FILE_PATH'" # Debugging
    continue # Go to the next line
  fi

  # Detect the start/end of a code block (e.g., ```php)
  if [[ "$line" =~ ^\`\`\`[a-z]*$ ]]; then
    echo "DEBUG: Detected code block delimiter: '$line'" # Debugging
    if ! "$COLLECTING_CONTENT"; then
      # This is the opening ```
      COLLECTING_CONTENT=true
      echo "DEBUG: Started collecting content." # Debugging
    else
      # This is the closing ``` - write the file content
      write_current_file
      echo "DEBUG: Stopped collecting content." # Debugging
    fi
    continue # Go to the next line
  fi

  # If we are inside a code block (between ``` and ```), append the line to content
  if "$COLLECTING_CONTENT"; then
    FILE_CONTENT+="$line"$'\n' # Append line and add a newline
    # echo "DEBUG: Appending line to content. Current content length: ${#FILE_CONTENT}" # Verbose debugging
  fi
done < "$INPUT_FILE" # Read from the absolute path of the input file

echo "DEBUG: Exited file reading loop." # Debugging

# Handle the very last file in the input file, if any content was collected
write_current_file

# --- Post-extraction Steps ---
echo "Making install.sh executable..."
if [ -f "install.sh" ]; then
  chmod +x install.sh
  echo "  'install.sh' is now executable."
else
  echo "Warning: 'install.sh' not found. Ensure it was extracted correctly."
fi

echo "Extraction complete! The project files are now in the '$PROJECT_ROOT/' directory."
echo "Navigate into '$PROJECT_ROOT/' and run './install.sh' to set up the database and dependencies."
