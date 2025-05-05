import React, { useState } from 'react';
import ChessSimilaritySearch from './components/ChessSimilaritySearch';
import GameInsights from './components/GameInsights';
import chessLogo from './assets/chess-logo-color.png';

const App = () => {
  const [activeTab, setActiveTab] = useState<'similarity' | 'insights'>('similarity');

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900">
      {/* Header Bar */}
      <header className="flex flex-col items-center px-6 py-4 bg-white shadow-sm border-b">
      <div className="flex items-center gap-2 mb-2">
        <img src={chessLogo} alt="Chess Logo" className="w-6 h-6" />
        <h1 className="text-xl font-bold tracking-wide">ChessTrainer</h1>
      </div>
  <div className="flex justify-center">
    <nav className="flex gap-8 text-sm font-medium">
      <button
        onClick={() => setActiveTab('similarity')}
        className={`border-b-2 pb-1 ${
          activeTab === 'similarity' ? 'border-blue-600 text-blue-600' : 'border-transparent'
        } hover:text-blue-500`}
      >
        Similarity Search
      </button>
      <button
        onClick={() => setActiveTab('insights')}
        className={`border-b-2 pb-1 ${
          activeTab === 'insights' ? 'border-blue-600 text-blue-600' : 'border-transparent'
        } hover:text-blue-500`}
      >
        Game Insights
      </button>
    </nav>
  </div>
</header>

      {/* Main Content */}
      <main className="p-6 max-w-7xl mx-auto">
        {activeTab === 'similarity' ? <ChessSimilaritySearch /> : <GameInsights />}
      </main>
    </div>
  );
};

export default App;