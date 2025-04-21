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
      positionId
      gameId
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
  const [formError, setFormError] = useState('');

  const [filters, setFilters] = useState({
    color: 'WHITE',
    selectedPieces: ['PAWN'],
    minElo: 1000,
    maxElo: 2000,
    limit: 12,
  });

  const [fetchSimilar, { loading, data, error: gqlError }] = useLazyQuery(FIND_SIMILAR_POSITIONS);

  const handleSubmit = () => {
    if (!fen) {
      setFormError('Please set up a position on the board or via FEN.');
      return;
    }

    if (filters.selectedPieces.length === 0) {
      setFormError('Please select at least one piece type.');
      return;
    }

    if (
      filters.minElo < 500 ||
      filters.maxElo > 2500 ||
      filters.minElo > filters.maxElo
    ) {
      setFormError('ELO must be between 500–2500 and minElo should be ≤ maxElo.');
      return;
    }

    if (filters.limit < 1 || filters.limit > 30) {
      setFormError('Result limit must be between 1 and 30.');
      return;
    }

    setFormError('');

    fetchSimilar({
      variables: {
        fen,
        request: {
          color: filters.color,
          selectedPieces: filters.selectedPieces,
          minElo: filters.minElo,
          maxElo: filters.maxElo,
          limit: filters.limit,
        },
      },
      fetchPolicy: 'network-only',
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

        {formError && (
          <p className="text-red-600 font-medium mt-2">{formError}</p>
        )}
      </div>

      <hr className="my-6" />

      {loading && <p>Loading results...</p>}
      {gqlError && <p className="text-red-500 mt-2">Backend Error: {gqlError.message}</p>}

      <ResultsDisplay
        results={data?.findSimilarPositionsByFen || []}
        userSide={filters.color.toLowerCase()} // white or black
        isFetchingPositions={loading}
      />
    </div>
  );
};

export default App;
