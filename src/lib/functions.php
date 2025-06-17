<?php
// src/lib/functions.php

// Create database connection
function connect_db() {
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
    if ($conn->connect_error) {
        die("Connection failed: " . $conn->connect_error);
    }
    return $conn;
}

// Basic Sanitization
function sanitize_input($data) {
    $data = trim($data);
    $data = stripslashes($data);
    $data = htmlspecialchars($data);
    return $data;
}

// Function to generate a thumbnail (simplified)
function generate_thumbnail($source, $destination, $width, $height) {
    $supported_types = ['image/jpeg', 'image/png', 'image/gif'];
     // Get the image type
    $image_info = getimagesize($source);
    if ($image_info === false) {
        return false; // Not a valid image.
    }
    $mime_type = $image_info['mime'];

    if (!in_array($mime_type, $supported_types)) {
        return false;
    }
    switch ($mime_type) {
        case 'image/jpeg':
            $image = imagecreatefromjpeg($source);
            break;
        case 'image/png':
            $image = imagecreatefrompng($source);
            break;
        case 'image/gif':
            $image = imagecreatefromgif($source);
            break;
        default:
            return false;
    }


    if (!$image) return false;

    $thumb = imagecreatetruecolor($width, $height);
    if (!$thumb) return false;

    // Preserve transparency if possible
    if ($mime_type === 'image/png' || $mime_type === 'image/gif') {
        imagealphablending($thumb, false);
        imagesavealpha($thumb, true);
        $transparent = imagecolorallocatealpha($thumb, 255, 255, 255, 127);
        imagefill($thumb, 0, 0, $transparent);
    }

    imagecopyresampled($thumb, $image, 0, 0, 0, 0, $width, $height, imagesx($image), imagesy($image));
    // Determine the correct image type for saving
    $file_extension = pathinfo($destination, PATHINFO_EXTENSION);
     switch ($file_extension) {
        case 'jpg':
        case 'jpeg':
             imagejpeg($thumb, $destination);
             break;
        case 'png':
             imagepng($thumb, $destination);
             break;
        case 'gif':
             imagegif($thumb, $destination);
             break;
        default:
            imagedestroy($image);
            imagedestroy($thumb);
            return false;
    }
    imagedestroy($image);
    imagedestroy($thumb);
    return true;
}

// New helper function to get or create an ID for related entities (stores, authors, licenses, tags, projects)
function get_or_create_id($table_name, $column_name, $value, $conn) {
    if (empty($value)) {
        return null; // Return null if value is empty, allows nullable foreign keys
    }

    // Sanitize the value for safe database query
    $sanitized_value = $conn->real_escape_string($value);

    // Dynamically construct the singular ID column name
    $id_column_name = rtrim($table_name, 's') . '_id';

    $stmt = $conn->prepare("SELECT " . $id_column_name . " FROM " . $table_name . " WHERE " . $column_name . " = ?");
    if ($stmt === false) {
        error_log("Prepare failed for get_or_create_id ($table_name): " . $conn->error);
        return null;
    }
    $stmt->bind_param("s", $sanitized_value);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $stmt->close();
        return $row[$id_column_name]; // Use the corrected ID column name
    } else {
        $stmt->close();
        $stmt = $conn->prepare("INSERT INTO " . $table_name . " (" . $column_name . ") VALUES (?)");
        if ($stmt === false) {
            error_log("Prepare failed for insert ($table_name): " . $conn->error);
            return null;
        }
        $stmt->bind_param("s", $sanitized_value);
        if ($stmt->execute()) {
            $new_id = $conn->insert_id;
            $stmt->close();
            return $new_id;
        } else {
            error_log("Insert failed for ($table_name): " . $stmt->error);
            $stmt->close();
            return null;
        }
    }
}

// Function to associate tags with an asset
function associate_tags_with_asset($asset_id, $tags_string, $conn) {
    $tags = array_filter(array_map('trim', explode(',', $tags_string)));
    foreach ($tags as $tag_name) {
        if (empty($tag_name)) continue; // Skip empty tag names

        $tag_id = get_or_create_id('tags', 'tag_name', $tag_name, $conn);
        if ($tag_id) {
            // Check if association already exists to prevent duplicates
            $stmt = $conn->prepare("SELECT * FROM asset_tags WHERE asset_id = ? AND tag_id = ?");
            if ($stmt === false) {
                error_log("Prepare failed for tag association check: " . $conn->error);
                continue;
            }
            $stmt->bind_param("ii", $asset_id, $tag_id);
            $stmt->execute();
            $result = $stmt->get_result();
            if ($result->num_rows == 0) {
                $stmt->close();
                $stmt = $conn->prepare("INSERT INTO asset_tags (asset_id, tag_id) VALUES (?, ?)");
                if ($stmt === false) {
                    error_log("Prepare failed for tag association insert: " . $conn->error);
                    continue;
                }
                $stmt->bind_param("ii", $asset_id, $tag_id);
                if (!$stmt->execute()) {
                    error_log("Error associating tag ($tag_name) with asset ($asset_id): " . $stmt->error);
                }
            }
            $stmt->close();
        }
    }
}

// Function to associate projects with an asset
function associate_projects_with_asset($asset_id, $projects_string, $conn) {
    $projects = array_filter(array_map('trim', explode(',', $projects_string)));
    foreach ($projects as $project_name) {
        if (empty($project_name)) continue; // Skip empty project names

        $project_id = get_or_create_id('projects', 'project_name', $project_name, $conn);
        if ($project_id) {
            // Check if association already exists to prevent duplicates
            $stmt = $conn->prepare("SELECT * FROM asset_projects WHERE asset_id = ? AND project_id = ?");
            if ($stmt === false) {
                error_log("Prepare failed for project association check: " . $conn->error);
                continue;
            }
            $stmt->bind_param("ii", $asset_id, $project_id);
            $stmt->execute();
            $result = $stmt->get_result();
            if ($result->num_rows == 0) {
                $stmt->close();
                $stmt = $conn->prepare("INSERT INTO asset_projects (asset_id, project_id) VALUES (?, ?)");
                if ($stmt === false) {
                    error_log("Prepare failed for project association insert: " . $conn->error);
                    continue;
                }
                $stmt->bind_param("ii", $asset_id, $project_id);
                if (!$stmt->execute()) {
                    error_log("Error associating project ($project_name) with asset ($asset_id): " . $stmt->error);
                }
            }
            $stmt->close();
        }
    }
}

// New function to generate tags from filename patterns
function generate_filename_tags($asset_id, $full_file_path, $conn) {
    $file_name = basename($full_file_path);
    $name_without_ext = pathinfo($file_name, PATHINFO_FILENAME);

    // 1. Always add the full filename (without extension) as a tag
    associate_tags_with_asset($asset_id, $name_without_ext, $conn);

    // 2. Split by common delimiters and add as tags (filtered)
    $parts = preg_split('/[_-]/', $name_without_ext); // Split by underscore or hyphen
    foreach ($parts as $part) {
        $part = trim($part);
        if (empty($part)) continue;

        // Skip purely numeric parts (unless they are part of a specific pattern later)
        if (is_numeric($part) && strlen($part) < 3) { // Adjust length threshold as needed
            continue;
        }
        // Skip common generic words
        if (in_array(strtolower($part), ['a', 'the', 'and', 'or', 'to', 'of', 'for'])) {
            continue;
        }

        associate_tags_with_asset($asset_id, $part, $conn);
    }

    // 3. Detect and add dimension tags (e.g., 20x20, 1280x720)
    if (preg_match_all('/\b(\d{2,4}x\d{2,4})\b/i', $name_without_ext . ' ' . dirname($full_file_path), $matches)) {
        foreach ($matches[1] as $dimension_tag) {
            associate_tags_with_asset($asset_id, $dimension_tag, $conn);
        }
    }

    // 4. (Optional, future enhancement): Detect specific sequence patterns like "prefix:N"
    // For "1_fire_1.png", if you specifically want "prefix:1", "suffix:1"
    // This would require more specific regex and might be too complex for initial implementation.
    // E.g., if (preg_match('/^(\d+)_/', $name_without_ext, $m)) { associate_tags_with_asset($asset_id, 'prefix:' . $m[1], $conn); }
    // E.g., if (preg_match('/_(\d+)$/', $name_without_ext, $m)) { associate_tags_with_asset($asset_id, 'suffix:' . $m[1],
    // conn); }
}


// Updated process_zip_file to handle subdirectory tags and pass base tags
function process_zip_file($file_path, $asset_id, $conn, $base_tags_string = '') {
    $zip = new ZipArchive;
    if ($zip->open($file_path) === TRUE) {
        $num_files = $zip->count();
        $asset_upload_dir = UPLOAD_DIR . $asset_id . '/';
        if (!is_dir($asset_upload_dir)) {
            mkdir($asset_upload_dir, 0777, true);
        }

        // Apply base tags to the asset (e.g., from original zip filename, source URL)
        if (!empty($base_tags_string)) {
            associate_tags_with_asset($asset_id, $base_tags_string, $conn);
        }

        for ($i = 0; $i < $num_files; $i++) {
            $entry_name = $zip->getNameIndex($i);
            $entry_size = $zip->statIndex($i)['size'];

            if (substr($entry_name, -1) == '/') { // Skip directories
                continue;
            }

            $file_content = $zip->getFromIndex($i);
            $target_path = $asset_upload_dir . $entry_name;
            $target_dir = dirname($target_path);
            if (!is_dir($target_dir)) {
                mkdir($target_dir, 0777, true);
            }

            if ($file_content !== false && file_put_contents($target_path, $file_content) !== false) {
                $file_type = mime_content_type($target_path);
                $preview_path = null;

                // Handle image previews
                if (strpos($file_type, 'image/') === 0) {
                    $thumb_filename = basename($entry_name, '.' . pathinfo($entry_name, PATHINFO_EXTENSION)) . '.jpg';
                    $thumb_path = PREVIEW_DIR . $asset_id . '_' . $thumb_filename;
                    if (generate_thumbnail($target_path, $thumb_path, 200, 200)) {
                         $preview_path = '/previews/' . $asset_id . '_' . $thumb_filename;
                    }
                }
                // Text file handling: Just store the file, the asset_details.php template handles download link.
                // The ALLOWED_TYPES config now includes common text formats.

                // Store file details in database
                $stmt = $conn->prepare("INSERT INTO files (asset_id, file_name, file_path, file_size, file_type, preview_path) VALUES (?, ?, ?, ?, ?, ?)");
                if ($stmt === false) {
                    error_log("Prepare failed for file insert: " . $conn->error);
                    continue;
                }
                $stmt->bind_param("isssis", $asset_id, $entry_name, $target_path, $entry_size, $file_type, $preview_path);
                if (!$stmt->execute()) {
                    error_log("Error inserting file info for $entry_name: " . $stmt->error);
                }
                $stmt->close();

                // Generate tags from directory structure within the zip
                $relative_dir = dirname($entry_name);
                if ($relative_dir != '.' && $relative_dir != '') {
                    $dir_parts = array_filter(explode('/', $relative_dir));
                    foreach ($dir_parts as $part) {
                        associate_tags_with_asset($asset_id, $part, $conn);
                    }
                }

                // Generate tags from the filename of the extracted file itself
                generate_filename_tags($asset_id, $entry_name, $conn);

            } else {
                error_log("Failed to extract or write file: " . $entry_name);
            }
        }
        $zip->close();
        unlink($file_path); // Delete the original zip file after extraction
        return true;
    } else {
        error_log("Failed to open zip file: " . $file_path);
        return false;
    }
}


// Function to handle file uploads (now creates the asset record too)
function handle_file_upload($file, $asset_details, $conn) {
    // Extract individual asset details
    $asset_name = $asset_details['asset_name'];
    $link = $asset_details['link'];
    $store_name = $asset_details['store_name'];
    $author_name = $asset_details['author_name'];
    $license_name = $asset_details['license_name'];
    $tags_string = $asset_details['tags_string'];
    $projects_string = $asset_details['projects_string'];

    // Get or create IDs for related entities
    $store_id = get_or_create_id('stores', 'store_name', $store_name, $conn);
    $author_id = get_or_create_id('authors', 'author_name', $author_name, $conn);
    $license_id = get_or_create_id('licenses', 'license_name', $license_name, $conn);

    // Insert the asset record first
    $stmt = $conn->prepare("INSERT INTO assets (asset_name, link, store_id, author_id, license_id) VALUES (?, ?, ?, ?, ?)");
    if ($stmt === false) {
        error_log("Prepare failed for asset insert: " . $conn->error);
        return "Error preparing asset insert.";
    }
    $stmt->bind_param("ssiii", $asset_name, $link, $store_id, $author_id, $license_id);
    if (!$stmt->execute()) {
        error_log("Error inserting asset: " . $stmt->error);
        $stmt->close();
        return "Error creating asset record: " . $stmt->error;
    }
    $asset_id = $conn->insert_id;
    $stmt->close();

    // Associate initial tags and projects with the new asset
    associate_tags_with_asset($asset_id, $tags_string, $conn);
    associate_projects_with_asset($asset_id, $projects_string, $conn);

    if ($file['error'] == 0) {
        $file_name = basename($file['name']);
        $file_size = $file['size'];
        $file_tmp = $file['tmp_name'];
        $file_type = mime_content_type($file_tmp);
        $file_ext = strtolower(pathinfo($file_name, PATHINFO_EXTENSION));

        if ($file_size > MAX_FILE_SIZE) {
            return "File too large (max " . (MAX_FILE_SIZE / (1024 * 1024)) . "MB)";
        }

        if (!in_array($file_ext, ALLOWED_TYPES)) {
            return "Invalid file type: .$file_ext";
        }

        $target_dir = UPLOAD_DIR . $asset_id . '/';
        if (!is_dir($target_dir)) {
            mkdir($target_dir, 0777, true);
        }
        $target_path = $target_dir . $file_name;

        if (move_uploaded_file($file_tmp, $target_path)) {
            // Generate tags from the main uploaded filename and source URL domain
            $base_file_tags = '';
            // Add domain from link as a tag if available
            if (!empty($link)) {
                $host = parse_url($link, PHP_URL_HOST);
                if ($host) {
                    $base_file_tags .= $host . ',';
                }
            }
            // Generate other filename-based tags
            generate_filename_tags($asset_id, $file_name, $conn);


            if ($file_ext == 'zip') {
                // Pass initial and file-derived tags to zip processor
                if(process_zip_file($target_path, $asset_id, $conn, $tags_string . ',' . $base_file_tags)){
                    return "ZIP file extracted and processed for Asset ID: " . $asset_id;
                } else {
                    return "ZIP file extraction failed for Asset ID: " . $asset_id;
                }
            } else { // Handle single file upload
                $preview_path = null;
                if (strpos($file_type, 'image/') === 0) {
                    $thumb_filename = basename($file_name, '.' . pathinfo($file_name, PATHINFO_EXTENSION)) . '.jpg';
                    $thumb_path = PREVIEW_DIR . $asset_id . '_' . $thumb_filename;
                    if (generate_thumbnail($target_path, $thumb_path, 200, 200)) {
                        $preview_path = '/previews/' . $asset_id . '_' . $thumb_filename;
                    }
                }

                $stmt = $conn->prepare("INSERT INTO files (asset_id, file_name, file_path, file_size, file_type, preview_path) VALUES (?, ?, ?, ?, ?, ?)");
                if ($stmt === false) {
                    error_log("Prepare failed for file insert: " . $conn->error);
                    return "Error preparing file insert.";
                }
                $stmt->bind_param("isssis", $asset_id, $file_name, $target_path, $file_size, $file_type, $preview_path);

                if ($stmt->execute()) {
                    return "File uploaded and details saved for Asset ID: " . $asset_id;
                } else {
                    error_log("Database insertion failed for file: " . $stmt->error);
                    return "File uploaded, but database entry failed: " . $stmt->error;
                }
                $stmt->close();
            }
        } else {
            return "File upload failed";
        }
    } else {
        return "File upload error: " . $file['error'];
    }
}

// Get file details, used in asset display
function get_file_details($asset_id, $conn) {
    $file_details = array();
    $stmt = $conn->prepare("SELECT file_name, file_path, file_size, file_type, preview_path FROM files WHERE asset_id = ?");
    $stmt->bind_param("i", $asset_id);
    $stmt->execute();
    $result = $stmt->get_result();
    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            $file_details[] = $row;
        }
    }
    $stmt->close();
    return $file_details;
}

// Get tag details for an asset
function get_tag_details($asset_id, $conn) {
    $tag_details = array();
    $stmt = $conn->prepare("SELECT t.tag_name FROM tags t JOIN asset_tags at ON t.tag_id = at.tag_id WHERE at.asset_id = ?");
    $stmt->bind_param("i", $asset_id);
    $stmt->execute();
    $result = $stmt->get_result();
    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            $tag_details[] = $row['tag_name'];
        }
    }
    $stmt->close();
    return $tag_details;
}

// Get project details for an asset.
function get_project_details($asset_id, $conn){
    $project_details = array();
    $stmt = $conn->prepare("SELECT p.project_name FROM projects p JOIN asset_projects ap ON p.project_id = ap.project_id WHERE ap.asset_id = ?");
    $stmt->bind_param("i", $asset_id);
    $stmt->execute();
    $result = $stmt->get_result();
    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            $project_details[] = $row['project_name'];
        }
    }
    $stmt->close();
    return $project_details;
}

// Placeholder: Get all assets
function get_all_assets($conn) {
    $assets = [];
    $sql = "SELECT asset_id, asset_name, link FROM assets ORDER BY asset_id DESC LIMIT 20"; // Example: get latest 20 assets
    $result = $conn->query($sql);
    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            $assets[] = $row;
        }
    }
    return $assets;
}

// Placeholder: Search assets
function search_assets($conn, $query) {
    $search_results = [];
    $search_query = "%" . $conn->real_escape_string($query) . "%";

    // Search by asset name
    $sql = "SELECT DISTINCT a.asset_id, a.asset_name, a.link FROM assets a WHERE a.asset_name LIKE '$search_query'";
    $result = $conn->query($sql);
    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            $search_results[$row['asset_id']] = $row; // Use asset_id as key to prevent duplicates
        }
    }

    // Search by tag name
    $sql_tags = "SELECT DISTINCT a.asset_id, a.asset_name, a.link FROM assets a
                 JOIN asset_tags at ON a.asset_id = at.asset_id
                 JOIN tags t ON at.tag_id = t.tag_id
                 WHERE t.tag_name LIKE '$search_query'";
    $result_tags = $conn->query($sql_tags);
    if ($result_tags->num_rows > 0) {
        while ($row = $result_tags->fetch_assoc()) {
            $search_results[$row['asset_id']] = $row;
        }
    }

    // Search by file type (e.g., 'image/png', 'audio/mp3', 'text/plain')
    $sql_file_type = "SELECT DISTINCT a.asset_id, a.asset_name, a.link FROM assets a
                      JOIN files f ON a.asset_id = f.asset_id
                      WHERE f.file_type LIKE '$search_query'";
    $result_file_type = $conn->query($sql_file_type);
    if ($result_file_type->num_rows > 0) {
        while ($row = $result_file_type->fetch_assoc()) {
            $search_results[$row['asset_id']] = $row;
        }
    }

    // Search by graphics size (e.g., '20x20') - This relies on dimension tags being added
    $sql_size = "SELECT DISTINCT a.asset_id, a.asset_name, a.link FROM assets a
                 JOIN asset_tags at ON a.asset_id = at.asset_id
                 JOIN tags t ON at.tag_id = t.tag_id
                 WHERE t.tag_name LIKE '%x%' AND t.tag_name LIKE '$search_query'"; // Simplified size search
    $result_size = $conn->query($sql_size);
    if ($result_size->num_rows > 0) {
        while ($row = $result_size->fetch_assoc()) {
            $search_results[$row['asset_id']] = $row;
        }
    }


    // Convert to indexed array for templates
    return array_values($search_results);
}


// Placeholder: Get asset details
function get_asset_details($asset_id, $conn) {
    $asset_details = null;
    $stmt = $conn->prepare("SELECT a.asset_id, a.asset_name, a.link, s.store_name, auth.author_name, l.license_name
                            FROM assets a
                            LEFT JOIN stores s ON a.store_id = s.store_id
                            LEFT JOIN authors auth ON a.author_id = auth.author_id
                            LEFT JOIN licenses l ON a.license_id = l.license_id
                            WHERE a.asset_id = ?");
    $stmt->bind_param("i", $asset_id);
    $stmt->execute();
    $result = $stmt->get_result();
    if ($result->num_rows > 0) {
        $asset_details = $result->fetch_assoc();
    }
    $stmt->close();
    return $asset_details;
}
?>