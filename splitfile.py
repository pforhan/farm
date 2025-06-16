import os
import re
import sys

def extract_farm_project(input_file_path_raw):
    """
    Extracts the Farm Project files from a single text file.
    Assumes file content starts immediately after its path line,
    and ends when a new file path line is encountered.
    """
    project_root = "."
    # above was = "farm"

    # --- Pre-check and Setup ---
    if not input_file_path_raw:
        print("Usage: python extract_farm_project.py <input_file_path>")
        print("Please provide the path to the text file containing the project content.")
        sys.exit(1)

    # Resolve the absolute path of the input file
    input_file_path = os.path.realpath(input_file_path_raw)

    if not os.path.isfile(input_file_path):
        print(f"Error: Input file '{input_file_path}' not found or is not a regular file.")
        sys.exit(1)

    print(f"Starting extraction of Farm Project from '{input_file_path}'...")

    # Create the main project root directory if it doesn't exist
    os.makedirs(project_root, exist_ok=True)

    # Change to the project root directory so all subsequent paths are relative to it
    original_cwd = os.getcwd()
    os.chdir(project_root)
    print(f"Changed directory to: {os.getcwd()}") # Debugging: confirm current directory

    # Variables to track current file state
    current_file_path = None # Will be set to a string path
    file_content = [] # Using a list to store lines

    def write_current_file():
        nonlocal current_file_path, file_content
        print("DEBUG: Attempting to write file.") # Debugging
        if current_file_path and file_content:
            full_path = os.path.join(os.getcwd(), current_file_path)
            os.makedirs(os.path.dirname(full_path), exist_ok=True) # Ensure directory exists
            try:
                # Join lines and remove the trailing newline if it exists (from the loop's appending)
                content_to_write = "".join(file_content)
                if content_to_write.endswith('\n'):
                    content_to_write = content_to_write[:-1]

                with open(full_path, 'w', encoding='utf-8') as f:
                    f.write(content_to_write)
                print(f"  Created: {current_file_path}")
            except IOError as e:
                print(f"Error writing file {current_file_path}: {e}")
        else:
            print(f"DEBUG: No file to write or content is empty. Path: '{current_file_path}', Content length: {len(''.join(file_content))}") # Debugging

        # Reset state for the next file
        current_file_path = None
        file_content = []

    print("DEBUG: Entering file reading loop.") # Debugging
    try:
        with open(input_file_path, 'r', encoding='utf-8') as f:
            for line_num, line in enumerate(f, 1):
                # Clean line for matching (remove leading/trailing whitespace)
                stripped_line = line.strip()

                # Detect file path markers (e.g., `farm/config/config.php.dist`)
                # It should literally be `farm/` followed by anything, at the start of the line.
                match_file_path = re.match(r"^(farm/.*)$", stripped_line)

                if match_file_path:
                    print(f"DEBUG: Detected file path marker (line {line_num}): '{stripped_line}'") # Debugging
                    write_current_file() # Write content of previous file if any

                    current_file_path = match_file_path.group(1)
                    # Remove the 'farm/' prefix as we are already inside the 'farm' directory
                    current_file_path = current_file_path.replace("farm/", "", 1)
                    print(f"DEBUG: New file path set to: '{current_file_path}'") # Debugging
                    continue # Go to the next line, as this line was just a path marker

                # If we have a current_file_path set, and the line is not a new file path marker,
                # then it must be content for the current file.
                if current_file_path is not None:
                    file_content.append(line) # Append the line as is, with its newline intact
                    # print(f"DEBUG: Appending line to content. Current content length: {len(''.join(file_content))}") # Verbose debugging
                else:
                    # This case handles lines before the very first file path, or empty lines between files
                    # You might want to ignore these or log them if they indicate an issue in the input format
                    if stripped_line: # Only print if it's not just an empty line
                        print(f"DEBUG: Ignoring line {line_num} (no current file selected): '{stripped_line}'")

    except FileNotFoundError:
        print(f"Error: Input file '{input_file_path}' not found during processing.")
        sys.exit(1)
    except Exception as e:
        print(f"An unexpected error occurred during file reading: {e}")
        sys.exit(1)

    print("DEBUG: Exited file reading loop.") # Debugging

    # Handle the very last file in the input file, if any content was collected
    write_current_file()

    # --- Post-extraction Steps ---
    os.chdir(original_cwd) # Change back to original directory for install.sh path
    print("Making install.sh executable...")
    install_script_path = os.path.join(project_root, "install.sh")
    if os.path.exists(install_script_path):
        os.chmod(install_script_path, 0o755) # Give execute permissions (rwxr-xr-x)
        print(f"  '{install_script_path}' is now executable.")
    else:
        print(f"Warning: '{install_script_path}' not found. Ensure it was extracted correctly.")

    print(f"Extraction complete! The project files are now in the '{project_root}/' directory.")
    print(f"Navigate into '{project_root}/' and run './install.sh' to set up the database and dependencies.")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Error: No input file provided.")
        print("Usage: python extract_farm_project.py <input_file_path>")
        sys.exit(1)
    extract_farm_project(sys.argv[1])
