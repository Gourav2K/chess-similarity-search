import React, { useState } from 'react';
import ChessEditor from './components/ChessEditor';
import Filters from './components/Filters';
import ResultsDisplay from './components/ResultsDisplay';
import { gql, useLazyQuery } from '@apollo/client';

const FIND_SIMILAR_POSITIONS = gql`
  query findSimilarPositionsByFen($fen: String!, $request: SimilarityRequestInput!) {
    findSimilarPositionsByFen(fen: $fen, request: $request) {
      similarityScore
      moveNumber
      position {
        fen
      }
      game {
        whiteName
        blackName
        result
        site
        blackElo
        whiteElo
      }
    }
  }
`;

const App = () => {
  const [fen, setFen] = useState('');
  const [filters, setFilters] = useState({ color: 'WHITE', selectedPieces: [] as string[] });

  const [fetchSimilar, { loading, data, error }] = useLazyQuery(FIND_SIMILAR_POSITIONS);

  const handleSubmit = () => {
    if (!fen || filters.selectedPieces.length === 0) return;

    fetchSimilar({
      variables: {
        fen,
        request: {
          color: filters.color,
          selectedPieces: filters.selectedPieces,
          limit: 12,
        },
      },
    });
  };

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <h1 className="text-2xl font-bold mb-6">Chess Similarity Search</h1>

      <div className="flex flex-col md:flex-row gap-6">
        <div className="md:w-1/2">
          <ChessEditor onFenChange={setFen} />
        </div>
        <div className="md:w-1/2">
          <Filters onFiltersChange={setFilters} />
        </div>
      </div>

      <div className="my-4">
        <button
          onClick={handleSubmit}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
        >
          Search Similar Positions
        </button>
      </div>

      <hr className="my-6" />

      {loading && <p>Loading results...</p>}
      {error && <p className="text-red-500">Error: {error.message}</p>}

      <ResultsDisplay results={data?.findSimilarPositionsByFen || []} />
    </div>
  );
};

export default App;
