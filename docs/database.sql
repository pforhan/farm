CREATE TABLE stores (
    store_id INT AUTO_INCREMENT PRIMARY KEY,
    store_name VARCHAR(255) NOT NULL
);

CREATE TABLE licenses (
    license_id INT AUTO_INCREMENT PRIMARY KEY,
    license_name VARCHAR(255) NOT NULL
);

CREATE TABLE authors (
    author_id INT AUTO_INCREMENT PRIMARY KEY,
    author_name VARCHAR(255) NOT NULL
);

CREATE TABLE tags (
    tag_id INT AUTO_INCREMENT PRIMARY KEY,
    tag_name VARCHAR(255) NOT NULL UNIQUE KEY -- Tags should be unique
);

CREATE TABLE projects (
    project_id INT AUTO_INCREMENT PRIMARY KEY,
    project_name VARCHAR(255) NOT NULL UNIQUE KEY -- Projects should be unique
);

CREATE TABLE assets (
    asset_id INT AUTO_INCREMENT PRIMARY KEY,
    store_id INT,
    link VARCHAR(255),
    author_id INT,
    license_id INT,
    asset_name VARCHAR(255) NOT NULL,
    FOREIGN KEY (store_id) REFERENCES stores(store_id),
    FOREIGN KEY (author_id) REFERENCES authors(author_id),
    FOREIGN KEY (license_id) REFERENCES licenses(license_id)
);

CREATE TABLE files (
    file_id INT AUTO_INCREMENT PRIMARY KEY,
    asset_id INT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size INT NOT NULL,
    file_type VARCHAR(255) NOT NULL,
    preview_path VARCHAR(255), -- For storing the path to the generated thumbnail
    FOREIGN KEY (asset_id) REFERENCES assets(asset_id)
);

CREATE TABLE asset_tags (
    asset_id INT,
    tag_id INT,
    PRIMARY KEY (asset_id, tag_id),
    FOREIGN KEY (asset_id) REFERENCES assets(asset_id),
    FOREIGN KEY (tag_id) REFERENCES tags(tag_id)
);

CREATE TABLE asset_projects (
    asset_id INT,
    project_id INT,
    PRIMARY KEY (asset_id, project_id),
    FOREIGN KEY (asset_id) REFERENCES assets(asset_id),
    FOREIGN KEY (project_id) REFERENCES projects(project_id)
);