CREATE TABLE stores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    store_name VARCHAR(255) NOT NULL
);

CREATE TABLE licenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    license_name VARCHAR(255) NOT NULL
);

CREATE TABLE authors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    author_name VARCHAR(255) NOT NULL
);

CREATE TABLE tags (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tag_name VARCHAR(255) NOT NULL UNIQUE KEY
);

CREATE TABLE projects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_name VARCHAR(255) NOT NULL UNIQUE KEY
);

CREATE TABLE assets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    store_id INT,
    link VARCHAR(255),
    author_id INT,
    license_id INT,
    asset_name VARCHAR(255) NOT NULL,
    FOREIGN KEY (store_id) REFERENCES stores(id),
    FOREIGN KEY (author_id) REFERENCES authors(id),
    FOREIGN KEY (license_id) REFERENCES licenses(id)
);

CREATE TABLE files (
    id INT AUTO_INCREMENT PRIMARY KEY,
    asset_id INT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size INT NOT NULL,
    file_type VARCHAR(255) NOT NULL,
    preview_path VARCHAR(255), -- For storing the path to the generated thumbnail
    FOREIGN KEY (asset_id) REFERENCES assets(id)
);

CREATE TABLE asset_tags (
    asset_id INT,
    tag_id INT,
    PRIMARY KEY (asset_id, tag_id),
    FOREIGN KEY (asset_id) REFERENCES assets(id),
    FOREIGN KEY (tag_id) REFERENCES tags(id)
);

CREATE TABLE asset_projects (
    asset_id INT,
    project_id INT,
    PRIMARY KEY (asset_id, project_id),
    FOREIGN KEY (asset_id) REFERENCES assets(id),
    FOREIGN KEY (project_id) REFERENCES projects(id)
);
