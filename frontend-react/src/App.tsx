import { useState, useEffect } from 'react';
import { UploadForm } from './components/UploadForm';
import { BrowseAssets } from './components/BrowseAssets';
import { AssetDetailsScreen } from './components/AssetDetailsScreen';
import { EditAssetScreen } from './components/EditAssetScreen';
import { SearchResultsScreen } from './components/SearchResultsScreen';
import { AlertTriangle, Home, List, Search, Upload } from 'lucide-react'; // Import icons

// Define possible application views/pages
type Screen =
  | { name: 'Upload' }
  | { name: 'Browse' }
  | { name: 'AssetDetails'; assetId: number }
  | { name: 'EditAsset'; assetId: number }
  | { name: 'SearchResults'; query: string };

function App() {
  const [currentScreen, setCurrentScreen] = useState<Screen>({ name: 'Browse' });
  const [message, setMessage] = useState<string | null>(null);
  const [isError, setIsError] = useState<boolean>(false);
  const [searchQuery, useStateSearchQuery] = useState<string>('');

  // Global message display effect
  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => {
        setMessage(null);
        setIsError(false); // Reset error status
      }, 3000); // Clear message after 3 seconds
      return () => clearTimeout(timer);
    }
  }, [message]);

  const handleMessage = (msg: string, error: boolean = false) => {
    setMessage(msg);
    setIsError(error);
  };

  // Function to navigate to a screen
  const navigate = (screen: Screen) => {
    setCurrentScreen(screen);
    setMessage(null); // Clear messages on navigation
    setIsError(false);
  };

  const handleSearchSubmit = () => {
    if (searchQuery.trim() !== '') {
      navigate({ name: 'SearchResults', query: searchQuery.trim() });
    } else {
      handleMessage('Please enter a search query.', true);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 p-4 font-inter">
      <div className="bg-white rounded-lg shadow-lg p-6 max-w-4xl mx-auto">
        {/* Navigation Bar */}
        <nav className="flex flex-col sm:flex-row justify-between items-center mb-6 space-y-4 sm:space-y-0">
          <div className="flex space-x-2">
            <button
              onClick={() => navigate({ name: 'Browse' })}
              className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 flex items-center space-x-2 transition duration-200"
            >
              <List size={18} />
              <span>Browse Assets</span>
            </button>
            <button
              onClick={() => navigate({ name: 'Upload' })}
              className="px-4 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-opacity-50 flex items-center space-x-2 transition duration-200"
            >
              <Upload size={18} />
              <span>Upload New Asset</span>
            </button>
          </div>

          <div className="flex items-center space-x-2 w-full sm:w-auto">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => useStateSearchQuery(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  handleSearchSubmit();
                }
              }}
              placeholder="Search assets..."
              className="flex-grow px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
            />
            <button
              onClick={handleSearchSubmit}
              className="px-4 py-2 bg-purple-500 text-white rounded-md hover:bg-purple-600 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-opacity-50 flex items-center space-x-2 transition duration-200"
            >
              <Search size={18} />
              <span>Search</span>
            </button>
          </div>
        </nav>

        {/* Global Message Display */}
        {message && (
          <div
            className={`p-3 mb-4 rounded-md flex items-center space-x-2 ${
              isError ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'
            }`}
          >
            {isError ? <AlertTriangle size={20} /> : <Home size={20} />}
            <p className="font-medium">{message}</p>
          </div>
        )}

        {/* Main Content Area */}
        <div>
          {currentScreen.name === 'Upload' && (
            <UploadForm
              onUploadSuccess={(assetId, msg) => {
                handleMessage(msg);
                navigate({ name: 'AssetDetails', assetId });
              }}
              onUploadError={(errorMsg) => handleMessage(`Upload Error: ${errorMsg}`, true)}
            />
          )}
          {currentScreen.name === 'Browse' && (
            <BrowseAssets
              onAssetClick={(assetId) => navigate({ name: 'AssetDetails', assetId })}
              onEditClick={(assetId) => navigate({ name: 'EditAsset', assetId })}
              onMessage={handleMessage}
            />
          )}
          {currentScreen.name === 'AssetDetails' && (
            <AssetDetailsScreen
              assetId={currentScreen.assetId}
              onBackClick={() => navigate({ name: 'Browse' })}
              onEditClick={(assetId) => navigate({ name: 'EditAsset', assetId })}
              onMessage={handleMessage}
            />
          )}
          {currentScreen.name === 'EditAsset' && (
            <EditAssetScreen
              assetId={currentScreen.assetId}
              onUpdateSuccess={(updatedAssetId, msg) => {
                handleMessage(msg);
                navigate({ name: 'AssetDetails', assetId: updatedAssetId });
              }}
              onCancel={() => navigate({ name: 'AssetDetails', assetId: currentScreen.assetId })}
              onMessage={handleMessage}
            />
          )}
          {currentScreen.name === 'SearchResults' && (
            <SearchResultsScreen
              query={currentScreen.query}
              onAssetClick={(assetId) => navigate({ name: 'AssetDetails', assetId })}
              onEditClick={(assetId) => navigate({ name: 'EditAsset', assetId })}
              onMessage={handleMessage}
            />
          )}
        </div>
      </div>
    </div>
  );
}

export default App;