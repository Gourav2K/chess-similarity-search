import React, { useState, useEffect } from 'react';
import { Chess } from 'chess.js';
import { Chessboard } from 'react-chessboard';
import { Button } from '@/components/ui/button';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent } from '@/components/ui/card';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-react';

const GameInsights = () => {
  const [pgnInput, setPgnInput] = useState('');
  const [side, setSide] = useState('white');
  const [gameLoaded, setGameLoaded] = useState(false);
  const [insights, setInsights] = useState('');
  const [loading, setLoading] = useState(false);

  const [game, setGame] = useState(new Chess());
  const [fen, setFen] = useState(game.fen());
  const [moves, setMoves] = useState<string[]>([]);
  const [moveIndex, setMoveIndex] = useState(0);

  const handleSubmit = async () => {
    if (!pgnInput.trim()) return;
    setLoading(true);
    setInsights('');

    const response = await fetch('http://localhost:8000/analyze-single-strategy', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ gameId: 'ui-analysis', fen: '', moves: pgnInput, side })
    });

    const data = await response.json();
    setInsights(data.summary);

    const g = new Chess();
    g.loadPgn(pgnInput);
    const history = g.history();
    setMoves(history);
    g.reset();
    setGame(g);
    setFen(g.fen());
    setMoveIndex(0);

    setGameLoaded(true);
    setLoading(false);
  };

  const reset = () => {
    setPgnInput('');
    setSide('white');
    setGameLoaded(false);
    setInsights('');
    setMoves([]);
    setMoveIndex(0);
    const freshGame = new Chess();
    setGame(freshGame);
    setFen(freshGame.fen());
  };

  const handleNext = () => {
    if (moveIndex < moves.length) {
      const g = new Chess();
      g.loadPgn(moves.slice(0, moveIndex + 1).join(' '));
      setFen(g.fen());
      setMoveIndex(moveIndex + 1);
    }
  };

  const handleBack = () => {
    if (moveIndex > 0) {
      const g = new Chess();
      g.loadPgn(moves.slice(0, moveIndex - 1).join(' '));
      setFen(g.fen());
      setMoveIndex(moveIndex - 1);
    }
  };

  return (
    <div className="flex flex-col md:flex-row gap-6">
      <div className="md:w-1/2 flex flex-col gap-4">
        {!gameLoaded ? (
          <>
            <Label htmlFor="pgn">Paste PGN of the game:</Label>
            <Textarea
              id="pgn"
              rows={8}
              placeholder="Paste PGN here..."
              value={pgnInput}
              onChange={(e) => setPgnInput(e.target.value)}
            />

            <RadioGroup value={side} onValueChange={setSide} className="flex gap-6">
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="white" id="white" />
                <Label htmlFor="white">White</Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="black" id="black" />
                <Label htmlFor="black">Black</Label>
              </div>
            </RadioGroup>

            <Button onClick={handleSubmit} disabled={loading}>
              {loading ? 'Analyzing...' : 'Get Insights'}
            </Button>
          </>
        ) : (
          <>
            <Card>
            <CardContent className="p-4 text-sm max-h-[45rem] overflow-auto w-full">
            <div className="prose prose-sm prose-headings:mb-2 prose-ul:ml-6 max-w-none">
                <ReactMarkdown remarkPlugins={[remarkGfm]}>
                {insights}
                </ReactMarkdown>
            </div>
            </CardContent>
            </Card>
            <Button
              onClick={reset}
              className="mt-4 bg-black text-white hover:bg-gray-800 transition duration-200 border border-black"
            >
              Analyze a New Game
            </Button>
          </>
        )}
      </div>

      <div className="md:w-1/2 flex flex-col items-center gap-4">
        {gameLoaded ? (
          <>
            <Chessboard position={fen} arePiecesDraggable={false} boardWidth={400} />
            <div className="flex justify-center gap-2 mt-2">
            <Button
                variant="outline"
                onClick={() => {
                const g = new Chess();
                setFen(g.fen());
                setMoveIndex(0);
                }}
                disabled={moveIndex === 0}
            >
                <ChevronsLeft className="w-4 h-4" />
            </Button>

            <Button
                variant="outline"
                onClick={handleBack}
                disabled={moveIndex === 0}
            >
                <ChevronLeft className="w-4 h-4" />
            </Button>

            <Button
                variant="outline"
                onClick={handleNext}
                disabled={moveIndex === moves.length}
            >
                <ChevronRight className="w-4 h-4" />
            </Button>

            <Button
                variant="outline"
                onClick={() => {
                const g = new Chess();
                g.loadPgn(moves.join(' '));
                setFen(g.fen());
                setMoveIndex(moves.length);
                }}
                disabled={moveIndex === moves.length}
            >
                <ChevronsRight className="w-4 h-4" />
            </Button>
            </div>
          </>
        ) : (
          <div className="w-full h-[400px] bg-gray-100 flex items-center justify-center text-gray-400 text-sm border border-dashed rounded">
            Chessboard preview will appear here after submission.
          </div>
        )}
      </div>
    </div>
  );
};

export default GameInsights;