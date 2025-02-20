import java.util.*;


// ==================== BLOCK CLASS ====================
class Block {
    private int value;
    private int x, y;

    public Block(int x, int y, int value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void doubleValue() {
        this.value *= 2;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}


// ==================== BLOCK FACTORY ====================
class BlockFactory {
    private static final Random random = new Random();

    public static Block createBlock(int x, int y) {
        return new Block(x, y, random.nextInt(10) < 9 ? 2 : 4);
    }
}


// ==================== MOVEMENT STRATEGY INTERFACE ====================
interface MoveStrategy {
    boolean move(Grid grid);
}


// ==================== MOVEMENT STRATEGY IMPLEMENTATIONS ====================
class MoveUp implements MoveStrategy {
    @Override
    public boolean move(Grid grid) {
        return grid.shiftAndMerge("up");
    }
}

class MoveDown implements MoveStrategy {
    @Override
    public boolean move(Grid grid) {
        return grid.shiftAndMerge("down");
    }
}

class MoveLeft implements MoveStrategy {
    @Override
    public boolean move(Grid grid) {
        return grid.shiftAndMerge("left");
    }
}

class MoveRight implements MoveStrategy {
    @Override
    public boolean move(Grid grid) {
        return grid.shiftAndMerge("right");
    }
}


// ==================== GRID CLASS ====================
class Grid {
    private final int size;
    private final Block[][] grid;
    private final Random random = new Random();

    public Grid(int size) {
        this.size = size;
        this.grid = new Block[size][size];
        spawnNewTile();
        spawnNewTile();
    }

    public void spawnNewTile() {
        List<int[]> emptyPositions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == null) {
                    emptyPositions.add(new int[]{i, j});
                }
            }
        }

        if (!emptyPositions.isEmpty()) {
            int[] pos = emptyPositions.get(random.nextInt(emptyPositions.size()));
            grid[pos[0]][pos[1]] = BlockFactory.createBlock(pos[0], pos[1]);
        }
    }

    public boolean isValidMove(String direction) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == null) continue;

                int[] newPos = getNewPosition(i, j, direction);
                int ni = newPos[0], nj = newPos[1];

                if (ni >= 0 && ni < size && nj >= 0 && nj < size) {
                    if (grid[ni][nj] == null || grid[ni][nj].getValue() == grid[i][j].getValue()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int[] getNewPosition(int i, int j, String direction) {
        switch (direction) {
            case "up": return new int[]{i - 1, j};
            case "down": return new int[]{i + 1, j};
            case "left": return new int[]{i, j - 1};
            case "right": return new int[]{i, j + 1};
            default: return new int[]{i, j};
        }
    }

    public boolean shiftAndMerge(String direction) {
        if (!isValidMove(direction)) return false;

        boolean[][] merged = new boolean[size][size];
        int start = (direction.equals("down") || direction.equals("right")) ? size - 1 : 0;
        int step = (direction.equals("down") || direction.equals("right")) ? -1 : 1;

        for (int i = start; i >= 0 && i < size; i += step) {
            for (int j = start; j >= 0 && j < size; j += step) {
                if (grid[i][j] == null) continue;

                int[] newPos = getNewPosition(i, j, direction);
                int ni = newPos[0], nj = newPos[1];

                while (ni >= 0 && ni < size && nj >= 0 && nj < size && grid[ni][nj] == null) {
                    grid[ni][nj] = grid[i][j];
                    grid[i][j] = null;
                    i = ni;
                    j = nj;
                    newPos = getNewPosition(i, j, direction);
                    ni = newPos[0];
                    nj = newPos[1];
                }

                if (ni >= 0 && ni < size && nj >= 0 && nj < size &&
                        grid[ni][nj] != null &&
                        grid[ni][nj].getValue() == grid[i][j].getValue() &&
                        !merged[ni][nj]) {
                    
                    grid[ni][nj].doubleValue();
                    grid[i][j] = null;
                    merged[ni][nj] = true;
                }
            }
        }

        spawnNewTile();
        return true;
    }

    public void printGrid() {
        for (Block[] row : grid) {
            for (Block block : row) {
                System.out.print((block == null ? 0 : block.getValue()) + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }
}


// ==================== GAME CONTROLLER ====================
class GameController {
    private final Grid grid;
    private final Map<String, MoveStrategy> moveStrategies;

    public GameController() {
        this.grid = new Grid(4);
        this.moveStrategies = new HashMap<>();
        moveStrategies.put("up", new MoveUp());
        moveStrategies.put("down", new MoveDown());
        moveStrategies.put("left", new MoveLeft());
        moveStrategies.put("right", new MoveRight());
    }

    public void playMove(String direction) {
        if (!moveStrategies.containsKey(direction)) {
            System.out.println("Invalid move");
            return;
        }

        if (moveStrategies.get(direction).move(grid)) {
            grid.printGrid();
        } else {
            System.out.println("Move not possible!");
        }
    }
}


// ==================== MAIN GAME LOOP ====================
public class Game2048 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GameController game = new GameController();

        while (true) {
            System.out.print("Enter move (up/down/left/right): ");
            String move = scanner.nextLine().trim().toLowerCase();
            game.playMove(move);
        }
    }
}
