
<?php
// Include the configuration file (outside web root)
require_once '../config/config.php';

// Include the functions file
require_once 'lib/functions.php';

session_start();

$conn = connect_db();

define('PROJECT_NAME', 'Farm');
define('PROJECT_ACRONYM', 'Files, Assets, Resources, Metadata');

$action = isset($_GET['action']) ? $_GET['action'] : 'upload';

switch ($action) {
    case 'upload':
        $message = '';
        if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_FILES['file'])) {
            $asset_details_input = [
                'asset_name'    => sanitize_input($_POST['asset_name'] ?? ''),
                'link'          => sanitize_input($_POST['link'] ?? ''),
                'store_name'    => sanitize_input($_POST['store_name'] ?? ''),
                'author_name'   => sanitize_input($_POST['author_name'] ?? ''),
                'license_name'  => sanitize_input($_POST['license_name'] ?? ''),
                'tags_string'   => sanitize_input($_POST['tags'] ?? ''),
                'projects_string' => sanitize_input($_POST['projects'] ?? '')
            ];
            $upload_result = handle_file_upload($_FILES['file'], $asset_details_input, $conn);
            $message = $upload_result;
        }
        include 'templates/upload_form.php';
        break;
    case 'browse':
        $assets = get_all_assets($conn);
        include 'templates/browse_assets.php';
        break;
    case 'search':
        $search_results = search_assets($conn, $_GET['query'] ?? '');
        include 'templates/search_results.php';
        break;
    case 'asset_details':
        $asset_id = isset($_GET['id']) ? intval($_GET['id']) : 0;
        if ($asset_id > 0) {
            $asset_details = get_asset_details($asset_id, $conn);
            $file_details = get_file_details($asset_id, $conn);
            $tag_details = get_tag_details($asset_id, $conn);
            $project_details = get_project_details($asset_id, $conn);
            include 'templates/asset_details.php';
        } else {
            echo "Invalid Asset ID";
        }
        break;
    default:
        echo "Invalid action.";
}

$conn->close();
