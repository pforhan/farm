
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo PROJECT_NAME; ?> - Browse Assets</title>
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <h1><?php echo PROJECT_NAME; ?> - Browse Assets</h1>
    <h2><?php echo PROJECT_ACRONYM; ?></h2>

    <p>
        <a href="index.php?action=upload">Upload New Asset</a> |
        <a href="index.php?action=search">Search Assets</a>
    </p>

    <?php if (!empty($assets)): ?>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Link</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <?php foreach ($assets as $asset): ?>
                    <tr>
                        <td><?php echo htmlspecialchars($asset['asset_id']); ?></td>
                        <td><?php echo htmlspecialchars($asset['asset_name']); ?></td>
                        <td>
                            <?php if (!empty($asset['link'])): ?>
                                <a href="<?php echo htmlspecialchars($asset['link']); ?>" target="_blank">View Link</a>
                            <?php else: ?>
                                N/A
                            <?php endif; ?>
                        </td>
                        <td><a href="index.php?action=asset_details&id=<?php echo $asset['asset_id']; ?>">Details</a></td>
                    </tr>
                <?php endforeach; ?>
            </tbody>
        </table>
    <?php else: ?>
        <p>No assets found. Start by <a href="index.php?action=upload">uploading one</a>!</p>
    <?php endif; ?>

    <script src="js/scripts.js"></script>
</body>
</html>
