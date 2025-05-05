import React, { useEffect, useState } from 'react';
import { Chessboard } from 'react-chessboard';
import { Chess } from 'chess.js';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Card, CardContent } from '@/components/ui/card';
import { Switch } from '@/components/ui/switch';

interface ChessEditorProps {
  onFenChange: (fen: string) => void;
}

const ChessEditor: React.FC<ChessEditorProps> = ({ onFenChange }) => {
  const [game] = useState(new Chess());
  const [fen, setFen] = useState(game.fen());
  const [useFenInput, setUseFenInput] = useState(false);

  useEffect(() => {
    onFenChange(fen); // Notify parent whenever FEN changes
  }, [fen, onFenChange]);

  const handlePieceDrop = (sourceSquare: string, targetSquare: string) => {
    const move = game.move({
      from: sourceSquare,
      to: targetSquare,
      promotion: 'q',
    });

    if (move === null) return false;

    const updatedFen = game.fen();
    setFen(updatedFen);
    return true;
  };

  const handleFenInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newFen = e.target.value;
    setFen(newFen);
    try {
      game.load(newFen);
    } catch {
      // Ignore invalid FEN but store it
    }
  };

  return (
    <Card className="mb-6">
      <CardContent className="space-y-4">

        <h2 className="text-xl font-semibold">Set up your position</h2>

        <div className="flex items-center space-x-2">
          <Switch
            id="fenToggle"
            checked={useFenInput}
            onCheckedChange={setUseFenInput}
          />
          <Label htmlFor="fenToggle">Use FEN input instead of board</Label>
        </div>

        {useFenInput ? (
          <div>
            <Label htmlFor="fenInput">FEN String</Label>
            <Input
              id="fenInput"
              type="text"
              value={fen}
              onChange={handleFenInputChange}
              placeholder="Enter FEN manually"
            />
          </div>
        ) : (
          <div className="mx-auto w-max">
            <Chessboard
              id="EditableBoard"
              position={fen}
              onPieceDrop={handlePieceDrop}
              boardWidth={400}
            />
          </div>
        )}

        <p className="text-sm text-gray-500">Current FEN: {fen}</p>
      </CardContent>
    </Card>
  );
};

export default ChessEditor;