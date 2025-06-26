import React, { useState, useEffect } from 'react';
import { Asset } from '../types/models';
import { FarmApiClient } from '../api/farmApiClient';
import { Loader2, Eye, Edit, Search as SearchIcon } from 'lucide-react'; // Icons

interface SearchResultsScreenProps {
  query: string;
  onAssetClick: (assetId: number) => void;
  onEditClick: (assetId: number) => void;
  onMessage: (msg: string, isError?: boolean) => void;
}

export const SearchResultsScreen: React.FC<SearchResultsScreenProps> = ({
  query: initialQuery,
  onAssetClick,
  onEditClick,
  onMessage,
}) => {
  const [currentQuery, setCurrentQuery] = useState<string>(initialQuery);
  const [searchResults, setSearchResults] = useState<Asset[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const performSearch = async (searchQuery: string) => {
    if (searchQuery.trim() === '') {
      onMessage('Search query cannot be empty.', true);
      setSearchResults([]);
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    try {
      const results = await FarmApiClient.searchAssets(searchQuery);
      setSearchResults(results);
      onMessage(`Found ${results.length} results for "${searchQuery}"`, false);
    } catch (e: any) {
      onMessage(`Search failed: ${e.message}`, true);
      setSearchResults([]);
    } finally {
      setIsLoading(false);
    }
  };

  // Perform initial search when component mounts or initial query changes
  useEffect(() => {
    performSearch(initialQuery);
  }, [initialQuery]); // Rerun effect if initialQuery prop changes

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    performSearch(currentQuery);
  };

  return (
    <div className="space-y-6">
      <h2 className="text-3xl font-bold text-gray-800 mb-4">Search Results</h2>

      {/* Search Input Field */}
      <form onSubmit={handleSearchSubmit} className="flex items-center space-x-2 mb-6">
        <input
          type="text"
          value={currentQuery}
          onChange={(e) => setCurrentQuery(e.target.value)}
          placeholder="Enter name, tag, type, or size"
          className="flex-grow px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
        />
        <button
          type="submit"
          className="px-4 py-2 bg-purple-500 text-white rounded-md hover:bg-purple-600 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-opacity-50 flex items-center space-x-2 transition duration-200"
        >
          <SearchIcon size={18} />
          <span>Search</span>
        </button>
      </form>

      <h3 className="text-xl font-semibold text-gray-700">Results for: "{currentQuery}"</h3>

      {isLoading ? (
        <div className="flex justify-center items-center h-64">
          <Loader2 className="animate-spin text-blue-500" size={48} />
        </div>
      ) : searchResults.length === 0 ? (
        <p className="text-lg text-gray-600 italic text-center p-4">No assets found matching your search query.</p>
      ) : (
        <div className="overflow-x-auto rounded-lg shadow-md">
          <table className="min-w-full bg-white border-collapse">
            <thead>
              <tr className="bg-gray-100 text-gray-600 uppercase text-sm leading-normal">
                <th className="py-3 px-6 text-left">ID</th>
                <th className="py-3 px-6 text-left">Preview</th>
                <th className="py-3 px-6 text-left">Name</th>
                <th className="py-3 px-6 text-left">Link</th>
                <th className="py-3 px-6 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="text-gray-700 text-sm font-light">
              {searchResults.map((asset) => (
                <tr key={asset.assetId} className="border-b border-gray-200 hover:bg-gray-50">
                  <td className="py-3 px-6 text-left whitespace-nowrap">{asset.assetId}</td>
                  <td className="py-3 px-6 text-left">
                    {asset.previewThumbnail ? (
                      <img
                        src={asset.previewThumbnail}
                        alt="Preview"
                        className="w-12 h-12 object-cover rounded-md shadow-sm"
                        onError={(e) => {
                          e.currentTarget.onerror = null;
                          e.currentTarget.src = `https://placehold.co/48x48/E2E8F0/64748B?text=N/A`;
                        }}
                      />
                    ) : (
                      <div className="w-12 h-12 bg-gray-200 flex items-center justify-center rounded-md text-xs text-gray-500">N/A</div>
                    )}
                  </td>
                  <td className="py-3 px-6 text-left">{asset.assetName}</td>
                  <td className="py-3 px-6 text-left">
                    {asset.link ? (
                      <a href={asset.link} target="_blank" rel="noopener noreferrer" className="text-blue-500 hover:underline">
                        View Link
                      </a>
                    ) : (
                      'N/A'
                    )}
                  </td>
                  <td className="py-3 px-6 text-center">
                    <div className="flex item-center justify-center space-x-2">
                      <button
                        onClick={() => onAssetClick(asset.assetId)}
                        className="flex items-center justify-center w-8 h-8 rounded-full bg-blue-100 text-blue-700 hover:bg-blue-200 transition duration-200 tooltip"
                        data-tooltip="Details"
                      >
                        <Eye size={16} />
                      </button>
                      <button
                        onClick={() => onEditClick(asset.assetId)}
                        className="flex items-center justify-center w-8 h-8 rounded-full bg-yellow-100 text-yellow-700 hover:bg-yellow-200 transition duration-200 tooltip"
                        data-tooltip="Edit"
                      >
                        <Edit size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};