<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo PROJECT_NAME; ?> - Search Results</title>
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <h1><?php echo PROJECT_NAME; ?> - Search Results</h1>
    <h2><?php echo PROJECT_ACRONYM; ?></h2>

    <?php include __DIR__ . '/partials/navigation.php'; ?>

    <form action="index.php" method="get">
        <input type="hidden" name="action" value="search">
        <label for="query">Search:</label>
        <input type="text" id="query" name="query" value="<?php echo htmlspecialchars($_GET['query'] ?? ''); ?>" placeholder="Enter name, tag, type, or size (e.g., 512x512)">
        <input type="submit" value="Search">
    </form>

    <h3>Search Query: "<?php echo htmlspecialchars($_GET['query'] ?? ''); ?>"</h3>

    <?php if (!empty($search_results)): ?>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Preview</th>
                    <th>Name</th>
                    <th>Link</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <?php foreach ($search_results as $asset): ?>
                    <tr>
                        <td><?php echo htmlspecialchars($asset['asset_id']); ?></td>
                        <td>
                            <?php if (!empty($asset['preview_thumbnail'])): ?>
                                <img src="<?php echo htmlspecialchars($asset['preview_thumbnail']); ?>" alt="Preview" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;">
                            <?php else: ?>
                                N/A
                            <?php endif; ?>
                        </td>
                        <td><?php echo htmlspecialchars($asset['asset_name']); ?></td>
                        <td>
                            <?php if (!empty($asset['link'])): ?>
                                <a href="<?php echo htmlspecialchars($asset['link']); ?>" target="_blank">View Link</a>
                            <?php else: ?>
                                N/A
                            <?php endif; ?>
                        </td>
                        <td>
                            <a href="index.php?action=asset_details&id=<?php echo $asset['asset_id']; ?>">Details</a> |
                            <a href="index.php?action=edit_asset&id=<?php echo $asset['asset_id']; ?>">Edit</a>
                        </td>
                    </tr>
                <?php endforeach; ?>
            </tbody>
        </table>
    <?php else: ?>
        <p>No assets found matching your search query.</p>
    <?php endif; ?>

    <script src="js/scripts.js"></script>
</body>
</html>