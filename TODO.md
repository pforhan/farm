# **Farm Digital Asset Manager: Future Enhancements (TODOs)**

This document outlines the key tasks remaining to fully realize the vision for the Farm Digital Asset Manager, specifically for the **Kotlin Ktor backend with React/TypeScript frontend**. These enhancements will improve functionality, user experience, and overall system robustness.

## **1\. Implement Comprehensive File Processing (Zip Extraction & Type Filtering)**

* **Current State (Ktor):** The backend currently treats uploaded ZIP files as single files and does not extract their contents. File upload types are not strictly filtered beyond basic client-side checks.  
* **Enhancement:** Develop robust server-side logic for file processing:  
  * **ZIP Archive Extraction:** When a ZIP file is uploaded, automatically extract its contents. Each extracted file should be individually tracked in the `files` table, maintaining its original path within the archive (to reconstruct the directory structure if needed).  
  * **Strict File Type Filtering:** Implement server-side validation to ensure only `ALLOWED_TYPES` (as defined in `config.php` in the old PHP version, or a new configuration in Kotlin) are accepted. Reject uploads of unauthorized file types.  
  * **Error Handling for File Processing:** Improve error reporting for failed extractions or invalid file types.

## **2\. Two-Stage File Upload & Metadata Finalization**

* **Current State (Ktor):** File upload and asset metadata creation are a single, immediate step.  
* **Enhancement:** Implement a more robust, two-stage upload process:  
  * **Stage 1 (Upload & Initial Processing):** Users upload files (including ZIP archives). The backend processes these files (e.g., extracts contents, generates initial previews, performs basic validation). The system temporarily stores these processed files and their preliminary metadata.  
  * **Stage 2 (Metadata Review & Finalization):** After initial processing, the user is presented with a review screen on the frontend. This screen displays the extracted files (if a ZIP was uploaded) and any automatically generated metadata (e.g., tags from filenames, dimensions). The user can then review, modify, and finalize the asset's descriptive metadata before it's permanently committed to the database. This allows for better error handling, more accurate data entry, and greater user control.

## **3\. Implement Automatic Tagging from File Metadata**

* **Current State (Ktor):** Assets are currently tagged only with the tags explicitly entered by the user in the upload form. Automatic tagging based on file properties (like filename, directory structure within zips, or source URL domain) is not implemented.  
* **Enhancement:** Reintroduce and enhance automatic tagging capabilities:  
  * **Filename-Based Tags:** Automatically generate tags from the asset's main filename (e.g., splitting by delimiters like `_`, `-`, or detecting patterns like `20x20`).  
  * **Directory Structure Tags (for Zips):** For files extracted from ZIP archives, automatically generate tags based on the directory names they were found in (e.g., `characters/hero/idle.png` could add "characters", "hero" as tags).  
  * **Source URL Domain Tags:** If a source URL (`link`) is provided, automatically extract and add its domain as a tag (e.g., `itch.io`, `opengameart.org`).  
  * **Content-Based Tags (Optional):** For text-based files, consider basic content analysis to suggest tags.

## **4\. User Management**

* **Current State (Ktor):** The application currently lacks any user authentication or authorization.  
* **Enhancement:** Integrate a complete user management system to enable multi-user functionality:  
  * **User Registration & Login:** Allow new users to sign up and existing users to log in securely.  
  * **Role-Based Access Control (RBAC):** Define different user roles (e.g., Administrator, Editor, Viewer) with distinct permissions.  
  * **Permissions System:** Implement granular permissions to control actions such as uploading new assets, editing existing metadata, deleting assets, and managing tags/projects.

## **5\. Enhanced Search Capabilities**

* **Current State (Ktor):** Basic search functionality exists, allowing searches by asset name, tags, and file type.  
* **Enhancement:** Expand the search features to provide more powerful and precise filtering options:  
  * **Advanced Filtering:** Allow users to filter by multiple criteria simultaneously (e.g., "images tagged '2D' AND 'character' uploaded in the last month").  
  * **Date Range Filtering:** Add options to filter assets by their upload date or last modification date.  
  * **Specific Metadata Filters:** Enable filtering by specific stores, authors, or licenses.  
  * **Improved Size/Dimension Search:** Enhance the ability to search for assets based on specific dimensions (e.g., "exactly 512x512", "larger than 1024x768").  
  * **Full-Text Search (Optional):** For text-based files (TXT, MD, HTML, JSON, XML), consider implementing full-text search capabilities on their content.

## **6\. Tag Management UI**

* **Current State (Ktor):** Tags are primarily created implicitly during asset upload or editing. There is no dedicated interface for managing them.  
* **Enhancement:** Develop a comprehensive user interface for tag management:  
  * **View All Tags:** A dedicated page or section to list all existing tags.  
  * **Create/Edit/Delete Tags:** Functionality to manually add new tags, modify existing tag names, and remove obsolete tags.  
  * **Merge Tags:** A feature to combine two or more similar tags into a single, canonical tag (e.g., merging "pixel-art" and "pixelart" into "pixelart").  
  * **Tag Usage Overview:** Display which assets are associated with each tag, allowing for better organization and cleanup.

## **7\. Asset Deletion (with File System Cleanup)**

* **Current State (Ktor):** The backend has a `deleteAsset` function, but the frontend does not fully implement the deletion flow, including confirmation and file system cleanup.  
* **Enhancement:** Implement a robust asset deletion feature that ensures data integrity and proper file system cleanup:  
  * **Frontend Confirmation:** Implement a user-friendly confirmation dialog to prevent accidental deletions.  
  * **Backend File System Cleanup:** Extend the `deleteAsset` function in the backend to not only remove database records but also delete the actual uploaded files and their generated preview images from the `public/uploads` and `public/previews` directories on the server. This prevents orphaned files and manages storage space.

## **8\. API Documentation**

* **Current State (Ktor):** No formal API documentation exists.  
* **Enhancement:** Generate comprehensive API documentation for the Kotlin Ktor backend. This could involve:  
  * **OpenAPI/Swagger:** Integrate a tool like OpenAPI (Swagger) to automatically generate interactive API documentation based on the Ktor routes.  
  * **Benefits:** This will significantly improve the maintainability of the backend and facilitate future development or integration with other systems.

