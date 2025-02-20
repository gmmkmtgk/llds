import random
from abc import ABC, abstractmethod

# ==================== BLOCK (TILE) CLASS ====================

class Block:
    """Represents a single tile in the 2048 game."""
    
    def __init__(self, x: int, y: int, value: int = 2):
        self.value = value
        self.position = (x, y)

    def update_value(self):
        """Double the value when merged."""
        self.value *= 2


# ==================== BLOCK FACTORY ====================

class BlockFactory:
    """Factory to create new blocks dynamically."""

    @staticmethod
    def create_block(x, y):
        return Block(x, y, random.choice([2, 4]))  # New blocks are usually 2 or 4


# ==================== MOVEMENT STRATEGY INTERFACE ====================

class MoveStrategy(ABC):
    """Abstract Strategy for movement directions."""

    @abstractmethod
    def move(self, grid):
        pass


# ==================== MOVEMENT STRATEGY IMPLEMENTATIONS ====================

class MoveUp(MoveStrategy):
    """Handles movement in the UP direction."""

    def move(self, grid):
        return grid.shift_and_merge(direction="up")


class MoveDown(MoveStrategy):
    """Handles movement in the DOWN direction."""

    def move(self, grid):
        return grid.shift_and_merge(direction="down")


class MoveLeft(MoveStrategy):
    """Handles movement in the LEFT direction."""

    def move(self, grid):
        return grid.shift_and_merge(direction="left")


class MoveRight(MoveStrategy):
    """Handles movement in the RIGHT direction."""

    def move(self, grid):
        return grid.shift_and_merge(direction="right")


# ==================== GRID CLASS ====================

class Grid:
    """Represents the 2048 game board."""
    
    def __init__(self, size: int = 4):
        self.size = size
        self.grid = [[None for _ in range(size)] for _ in range(size)]
        self.spawn_new_tile()
        self.spawn_new_tile()

    def spawn_new_tile(self):
        """Places a new tile in a random empty position."""
        empty_positions = [(i, j) for i in range(self.size) for j in range(self.size) if self.grid[i][j] is None]
        if empty_positions:
            x, y = random.choice(empty_positions)
            self.grid[x][y] = BlockFactory.create_block(x, y)

    def is_valid_move(self, direction):
        """Check if a move is possible in the given direction."""
        for i in range(self.size):
            for j in range(self.size):
                if self.grid[i][j] is None:
                    continue
                new_i, new_j = self.get_new_position(i, j, direction)
                if 0 <= new_i < self.size and 0 <= new_j < self.size:
                    if self.grid[new_i][new_j] is None or self.grid[new_i][new_j].value == self.grid[i][j].value:
                        return True
        return False

    def get_new_position(self, i, j, direction):
        """Get the new position after movement."""
        if direction == "up":
            return i - 1, j
        elif direction == "down":
            return i + 1, j
        elif direction == "left":
            return i, j - 1
        elif direction == "right":
            return i, j + 1
        return i, j

    def shift_and_merge(self, direction):
        """Shift and merge tiles in the given direction."""
        if not self.is_valid_move(direction):
            return False  # No movement possible

        merged = [[False] * self.size for _ in range(self.size)]
        range_order = range(self.size) if direction in ["down", "right"] else range(self.size - 1, -1, -1)

        for i in range_order:
            for j in range_order:
                if self.grid[i][j] is None:
                    continue
                ni, nj = self.get_new_position(i, j, direction)
                
                while 0 <= ni < self.size and 0 <= nj < self.size and self.grid[ni][nj] is None:
                    self.grid[ni][nj], self.grid[i][j] = self.grid[i][j], None
                    i, j = ni, nj
                    ni, nj = self.get_new_position(i, j, direction)

                if (0 <= ni < self.size and 0 <= nj < self.size and 
                    self.grid[ni][nj] is not None and 
                    self.grid[ni][nj].value == self.grid[i][j].value and 
                    not merged[ni][nj]):
                    
                    self.grid[ni][nj].update_value()
                    self.grid[i][j] = None
                    merged[ni][nj] = True

        self.spawn_new_tile()
        return True

    def print_grid(self):
        """Print the grid for debugging."""
        for row in self.grid:
            print([block.value if block else 0 for block in row])
        print("\n")


# ==================== GAME CONTROLLER ====================

class GameController:
    """Manages game state and player moves."""
    
    def __init__(self):
        self.grid = Grid()
        self.moves = {
            "up": MoveUp(),
            "down": MoveDown(),
            "left": MoveLeft(),
            "right": MoveRight()
        }

    def play_move(self, direction):
        """Process player move."""
        if direction not in self.moves:
            print("Invalid move")
            return

        if self.moves[direction].move(self.grid):
            self.grid.print_grid()
        else:
            print("Move not possible!")


# ==================== MAIN GAME LOOP ====================

if __name__ == "__main__":
    game = GameController()
    
    while True:
        move = input("Enter move (up/down/left/right): ").strip().lower()
        game.play_move(move)
