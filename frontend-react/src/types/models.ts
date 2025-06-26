// farm/frontend-react/src/types/models.ts
// These interfaces mirror the Kotlin data classes in your common module.

export interface Asset {
    assetId: number;
    assetName: string;
    link: string | null;
    storeName: string | null;
    authorName: string | null;
    licenseName: string | null;
    tags: string[];
    projects: string[];
    files: FileDetail[];
    previewThumbnail: string | null; // Public URL path to the main thumbnail
}

export interface FileDetail {
    fileId: number;
    assetId: number;
    fileName: string;
    filePath: string; // Server-side absolute path, primarily for backend use
    publicPath: string; // Public URL path for download
    fileSize: number;
    fileType: string;
    previewPath: string | null; // Public URL path for file-specific thumbnail
}

export interface UpdateAssetRequest {
    assetName: string;
    link: string | null;
    storeName: string | null;
    authorName: string | null;
    licenseName: string | null;
    tagsString: string | null;
    projectsString: string | null;
}

export interface UploadAssetRequest {
    assetName: string;
    link: string | null;
    storeName: string | null;
    authorName: string | null;
    licenseName: string | null;
    tags: string | null;
    projects: string | null;
}