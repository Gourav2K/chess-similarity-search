import React, { useState } from 'react';
import { Chessboard } from 'react-chessboard';
import lichessLogo from '../assets/lichess-black.svg';
import searchLogo from '../assets/magnifying-glass-solid.svg';
import searchLogoWhite from '../assets/magnifying-glass-solid-white.svg';
import { gql, useLazyQuery } from '@apollo/client';
import SummaryModal from './SummaryModal';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';

const GENERATE_SUMMARY_QUERY = gql`
  query GenerateSummary($positionIds: [ID!]!, $side: String!) {
    generateSummaryForPositions(positionIds: $positionIds, side: $side) {
      aggregatedSummary
      perGameSummaries {
        gameId
        summary
      }
    }
  }
`;

interface Result {
  similarityScore: number;
  moveNumber: number;
  positionId: string;
  gameId: string;
  position: {
    fen: string;
  };
  game: {
    whiteName: string;
    blackName: string;
    result: string;
    site: string;
    blackElo: number;
    whiteElo: number;
  };
}

interface Props {
  results: Result[];
  userSide: string;
  isFetchingPositions: boolean;
}

const ResultsDisplay: React.FC<Props> = ({ results, userSide, isFetchingPositions }) => {
  const [selectedGameId, setSelectedGameId] = useState<string | null>(null);
  const [selectedGameData, setSelectedGameData] = useState<Result | null>(null);
  const [summaryMap, setSummaryMap] = useState<Record<string, string>>({});
  const [aggregatedSummary, setAggregatedSummary] = useState<string>('');
  const [positionIds, setPositionIds] = useState<string[]>([]);
  const [side, setSide] = useState<string>('');

  const [generateSummary, { data: summaryData, loading: summaryLoading }] = useLazyQuery(
    GENERATE_SUMMARY_QUERY,
    {
      onCompleted: (data) => {
        const newMap: Record<string, string> = {};
        data.generateSummaryForPositions.perGameSummaries.forEach((summary: any) => {
          newMap[summary.gameId] = summary.summary;
        });
        setSummaryMap((prev) => ({ ...prev, ...newMap }));
        setAggregatedSummary(data.generateSummaryForPositions.aggregatedSummary);
      }
    }
  );

  const handleGenerateSummary = () => {
    if (positionIds.length && side) {
      generateSummary({ variables: { positionIds, side } });
    }
  };

  const handleSummaryClick = (gameId: string, gameData: Result) => {
    setSelectedGameId(gameId);
    setSelectedGameData(gameData);
  };

  React.useEffect(() => {
    if (results && results.length > 0) {
      const ids = results.map((r) => r.positionId);
      setPositionIds(ids);
      setSide(userSide);
    }
  }, [results]);

  if (isFetchingPositions) {
    return (
      <div className="mb-4 p-4 bg-gray-50 border border-gray-300 rounded text-gray-700 flex items-center gap-2">
        <img src={searchLogo} alt="Search" className="w-5 h-5" />
        Fetching similar positions...
      </div>
    );
  }

  if (!results || results.length === 0) {
    return <p>No results found.</p>;
  }

  return (
    <div>
      <h2 className="text-xl font-semibold mb-4">Similar Positions</h2>

      {positionIds.length > 0 && (
        <div className="mb-4">
          <Button onClick={handleGenerateSummary} className="flex items-center gap-2">
            <img src={searchLogoWhite} alt="Lichess" className="w-5 h-5" />
            Generate Strategic Summary
          </Button>
        </div>
      )}

      {summaryLoading && (
        <div className="mb-4 p-4 bg-gray-50 border border-gray-300 rounded text-gray-700 flex items-center gap-2">
          <img src={searchLogo} alt="Lichess" className="w-5 h-5" />
          Generating strategic summary...
        </div>
      )}

      {aggregatedSummary && (
        <Card className="mb-6 bg-yellow-50 border-l-4 border-yellow-400">
          <CardContent className="p-4">
            <h3 className="text-lg font-semibold text-yellow-800 mb-2 flex items-center gap-2">
              <img src={searchLogo} alt="Strategy" className="w-5 h-5" />
              Aggregated Strategy Summary
            </h3>
            <div className="prose prose-sm text-gray-800 max-w-none">
              <ReactMarkdown remarkPlugins={[remarkGfm]}>{aggregatedSummary}</ReactMarkdown>
            </div>
          </CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {results.map((result, index) => {
          const gameId = result.gameId;
          return (
            <Card
              key={index}
              className="min-h-[460px] transition-transform duration-200 hover:shadow-lg hover:-translate-y-1"
            >
              <CardContent className="p-4 flex flex-col justify-start">
                <div className="w-[320px] mx-auto">
                  <Chessboard
                    id={`mini-board-${index}`}
                    position={result.position.fen}
                    arePiecesDraggable={false}
                    boardWidth={320}
                  />
                  <div className="mt-3 text-sm text-gray-800 text-left">
                    <p><strong>Similarity:</strong> {(result.similarityScore * 100).toFixed(1)}%</p>
                    <p><strong>Move:</strong> {result.moveNumber}</p>
                    <p><strong>Game:</strong> {result.game.whiteName} vs {result.game.blackName}</p>
                    <p><strong>ELO:</strong> {result.game.whiteElo} - {result.game.blackElo}</p>
                    <p><strong>Result:</strong> {result.game.result}</p>

                    <div className="flex gap-2 mt-3">
                      <a href={result.game.site} target="_blank" rel="noopener noreferrer">
                        <Button className="bg-blue-600 hover:bg-blue-700">
                          <img src={lichessLogo} alt="Lichess" className="w-5 h-5" />
                          View on Lichess
                        </Button>
                      </a>

                      {summaryMap.hasOwnProperty(gameId!) && (
                        <Button
                          className="bg-green-600 hover:bg-green-700"
                          onClick={() => handleSummaryClick(gameId!, result)}
                        >
                          <img src={searchLogo} alt="Lichess" className="w-5 h-5" />
                          View Strategy
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      {selectedGameData && (
        <SummaryModal
          isOpen={!!selectedGameId && !!summaryMap[selectedGameId]}
          onClose={() => {
            setSelectedGameId(null);
            setSelectedGameData(null);
          }}
          gameId={selectedGameId || ''}
          summary={selectedGameId && summaryMap[selectedGameId] ? summaryMap[selectedGameId] : ''}
          positionFen={selectedGameData.position.fen}
          gameInfo={{
            whiteName: selectedGameData.game.whiteName,
            blackName: selectedGameData.game.blackName,
            whiteElo: selectedGameData.game.whiteElo,
            blackElo: selectedGameData.game.blackElo,
            result: selectedGameData.game.result,
            site: selectedGameData.game.site,
          }}
        />
      )}
    </div>
  );
};

export default ResultsDisplay;
