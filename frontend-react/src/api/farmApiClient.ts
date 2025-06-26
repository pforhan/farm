// farm/frontend-react/src/api/farmApiClient.ts
import { Asset, UpdateAssetRequest } from '../types/models';

const BASE_URL = '/api'; // Ktor backend routes start with /api

export const FarmApiClient = {
  /**
   * Fetches all assets from the backend.
   * @returns A promise that resolves to a list of Asset objects.
   */
  getAssets: async (): Promise<Asset[]> => {
    const response = await fetch(`${BASE_URL}/assets`);
    if (!response.ok) {
      throw new Error(`Failed to fetch assets: ${response.statusText}`);
    }
    return response.json();
  },

  /**
   * Fetches detailed information for a specific asset by its ID.
   * @param assetId The ID of the asset to fetch.
   * @returns A promise that resolves to an Asset object or null if not found.
   */
  getAssetDetails: async (assetId: number): Promise<Asset | null> => {
    const response = await fetch(`${BASE_URL}/assets/${assetId}`);
    if (response.status === 404) {
      return null;
    }
    if (!response.ok) {
      throw new Error(`Failed to fetch asset details: ${response.statusText}`);
    }
    return response.json();
  },

  /**
   * Searches for assets based on a query string.
   * @param query The search query string.
   * @returns A promise that resolves to a list of Asset objects.
   */
  searchAssets: async (query: string): Promise<Asset[]> => {
    const response = await fetch(`${BASE_URL}/assets/search?query=${encodeURIComponent(query)}`);
    if (!response.ok) {
      throw new Error(`Failed to search assets: ${response.statusText}`);
    }
    return response.json();
  },

  /**
   * Uploads a new asset including its file and metadata.
   * @param assetName The name of the asset.
   * @param link Optional link to the asset's source.
   * @param storeName Optional name of the store.
   * @param authorName Optional name of the author.
   * @param licenseName Optional name of the license.
   * @param tags Optional comma-separated tags.
   * @param projects Optional comma-separated projects.
   * @param file The File object to upload.
   * @returns A promise that resolves to a success message string.
   */
  uploadAsset: async (
    assetName: string,
    link: string | null,
    storeName: string | null,
    authorName: string | null,
    licenseName: string | null,
    tags: string | null,
    projects: string | null,
    file: File,
  ): Promise<string> => {
    const formData = new FormData();
    formData.append('asset_name', assetName);
    if (link) formData.append('link', link);
    if (storeName) formData.append('store_name', storeName);
    if (authorName) formData.append('author_name', authorName);
    if (licenseName) formData.append('license_name', licenseName);
    if (tags) formData.append('tags', tags);
    if (projects) formData.append('projects', projects);
    formData.append('file', file); // Append the actual file

    const response = await fetch(`${BASE_URL}/assets/upload`, {
      method: 'POST',
      body: formData, // FormData automatically sets 'Content-Type: multipart/form-data'
    });

    const textResponse = await response.text(); // Read as text, as backend returns string message
    if (!response.ok) {
      throw new Error(`Upload failed: ${textResponse}`);
    }
    return textResponse;
  },

  /**
   * Updates an existing asset's metadata.
   * @param assetId The ID of the asset to update.
   * @param request The UpdateAssetRequest object containing the new metadata.
   * @returns A promise that resolves to a success message string.
   */
  updateAsset: async (assetId: number, request: UpdateAssetRequest): Promise<string> => {
    const response = await fetch(`${BASE_URL}/assets/${assetId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    const textResponse = await response.text();
    if (!response.ok) {
      throw new Error(`Update failed: ${textResponse}`);
    }
    return textResponse;
  },
};