import java.util.Scanner;
import java.util.Set;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class homework {

    public static int bX, bY, bZ; //boundary cooridinates
    public static String type;
    public static Node start;
    public static Node end;
    public static int numOfNodes;
    public static Map<Node, ArrayList<Node>> map = new HashMap<Node, ArrayList<Node>>();
    public static Queue<Node> q;
    public static Map<Node, Node> parents = new HashMap<Node, Node>();
    public static FileWriter writer;

    public static void main(String[] args) throws FileNotFoundException, IOException{
        File f = new File("./sample/input8.txt");
        Scanner scanner = new Scanner(f);
        
        readFile(scanner);

        if(type.equals("BFS")){
            BFS();
        }else if(type.equals("UCS")){
            UCS();
        }else{
            AStar();
        }
    }

    public static class Node{
        private int x;
        private int y;
        private int z;
        public int cost;

        public Node(int x, int y, int z){
            this.x = x;
            this.y = y;
            this.z = z;
            this.cost = 0;
        }

        @Override
        public int hashCode(){
            return x + 100 * y + 10000 * z;
        }

        @Override
        public boolean equals(Object o){
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            Node node = (Node)o;
            return this.x == node.x && this.y == node.y && this.z == node.z;
        }

        public String toString(){
            return x + " " + y + " " + z; 
        }

        public boolean inBoundary(int x, int y, int z){
            boolean xT = this.x >= 0 && this.x < x;
            boolean yT = this.y >= 0 && this.y < y;
            boolean zT = this.z >= 0 && this.z < z;
            return xT && yT && zT;
        }

        public int distance(Node node){
            int xDis = Math.abs(node.x - this.x);
            int yDis = Math.abs(node.y - this.y);
            int zDis = Math.abs(node.z - this.z);
            int tot = xDis + yDis + zDis;
            if(tot == 0){
                return 0;
            }else if(tot == 1){
                return 10;
            }else{
                return 14;
            }
        }

        public int future(Node node){
            int sX = Math.abs(node.x - this.x);
            int sY = Math.abs(node.y - this.y);
            int sZ = Math.abs(node.z - this.z);
            
            int max = Math.max(sX, Math.max(sY,sZ));
            int min = Math.min(sX, Math.min(sY,sZ));
            int middle = sX - max + sY - min + sZ ;
            
            int ret = 0;
            if(middle != min){
                int sub = middle - min;
                ret += sub * 14;
                middle = min;
                max -= sub;
            }

            if(max >= 2 * min){
                ret += min * 14 * 2;
                ret += (max - 2 * min) * 10;
            }else{
                if(max % 2 == 0){
                    ret += (min + max/2) * 14;
                }else{
                    ret += (min + (max - 1)/2) * 14 + 10;
                }
            }
            return ret;
        }
    }

    public static void BFS() throws IOException{
        int cost = 0;
        q = new LinkedList<Node>();
        writer = new FileWriter(new File("output.txt"));
        q.add(start);
        while(!q.isEmpty()){
            Node curr = q.poll();
            ArrayList<Node> children = map.get(curr);
            if(children == null) continue;
            for(int i = 0; i < children.size(); i++){
                Node child = children.get(i);
                if(child.equals(end)){
                    parents.put(end, curr);
                    break;
                }
                if(!parents.containsKey(child)){
                    q.add(child);
                    parents.put(child, curr);
                }
            }
        }
        if(!parents.containsKey(end)){
            writer.write("FAIL");
        }else{
            Deque<Node> stack = new ArrayDeque<Node>();
            Node curr = end;
            while(!curr.equals(start)){
                stack.push(curr);
                curr = parents.get(curr);
                cost++;
            }
            stack.push(start);
            writer.write(cost + "\n");
            writer.write((cost + 1) + "\n");
            writer.write(stack.pop().toString() + " 0\n");
            while(!stack.isEmpty()){
                writer.write(stack.pop().toString() + " 1\n");
            }
        }
        writer.close();
        return;
    }

    public static void UCS() throws IOException{
        q = new PriorityQueue<Node>((o1, o2) -> (o1.cost - o2.cost));
        writer = new FileWriter(new File("output.txt"));
        q.add(start);
        while(!q.isEmpty()){
            Node curr = q.poll();
            if(curr.equals(end)){
                end.cost = curr.cost;
                break;
            }
            ArrayList<Node> children = map.get(curr);
            if(children == null) continue;
            for(int i = 0; i < children.size(); i++){
                Node child = children.get(i);
                int newCost = curr.cost + curr.distance(child);
                if(parents.containsKey(child)){
                    if(newCost < child.cost){
                        child.cost = newCost;
                        parents.put(child, curr);
                        q.remove(child);
                        q.add(child);
                    }
                }else{
                    child.cost = newCost;
                    parents.put(child, curr);
                    q.add(child);
                }
            }
        }
        if(!parents.containsKey(end)){
            writer.write("FAIL");
        }else{
            Deque<Node> stack = new ArrayDeque<Node>();
            Node curr = end;
            int steps = 1;
            while(!curr.equals(start)){
                stack.push(curr);
                curr = parents.get(curr);
                steps++;
            }
            Node head = start;
            writer.write(end.cost + "\n");
            writer.write(steps + "\n");
            writer.write(start.toString() + " 0\n");
            while(!stack.isEmpty()){
                int cost = head.distance(stack.peek());
                head = stack.pop();
                writer.write(head.toString() + " " + cost +"\n");
            }
        }
        writer.close();
        return;
    }

    public static void AStar() throws IOException{
        q = new PriorityQueue<Node>((o1, o2) -> (o1.cost - o2.cost + o1.future(end) - o2.future(end)));
        writer = new FileWriter(new File("output.txt"));
        q.add(start);
        while(!q.isEmpty()){
            Node curr = q.poll();
            if(curr.equals(end)){
                end.cost = curr.cost;
                break;
            }
            ArrayList<Node> children = map.get(curr);
            if(children == null) continue;
            for(int i = 0; i < children.size(); i++){
                Node child = children.get(i);
                int newCost = curr.cost + curr.distance(child);
                if(parents.containsKey(child)){
                    if(newCost < child.cost){
                        child.cost = newCost;
                        parents.put(child, curr);
                        q.remove(child);
                        q.add(child);
                    }
                }else{
                    child.cost = newCost;
                    parents.put(child, curr);
                    q.add(child);
                }
            }
        }
        if(!parents.containsKey(end)){
            writer.write("FAIL");
        }else{
            Deque<Node> stack = new ArrayDeque<Node>();
            Node curr = end;
            int steps = 1;
            while(!curr.equals(start)){
                stack.push(curr);
                curr = parents.get(curr);
                steps++;
            }
            Node head = start;
            writer.write(end.cost + "\n");
            writer.write(steps + "\n");
            writer.write(start.toString() + " 0\n");
            while(!stack.isEmpty()){
                int cost = head.distance(stack.peek());
                head = stack.pop();
                writer.write(head.toString() + " " + cost +"\n");
            }
        }
        writer.close();
        return;
    }

    public static void readFile(Scanner scanner){
        //read type
        type = scanner.nextLine();
        //read boundary
        bX = scanner.nextInt();
        bY = scanner.nextInt();
        bZ = scanner.nextInt();
        //read initial and end points
        start = new Node(scanner.nextInt(), scanner.nextInt(), scanner.nextInt());
        end = new Node(scanner.nextInt(), scanner.nextInt(), scanner.nextInt());
        numOfNodes = scanner.nextInt();
        scanner.nextLine();
        
        int i = 0;
        while(i < numOfNodes){
            if(scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] numbers = line.split(" ");
                int x = Integer.parseInt(numbers[0]); 
                int y = Integer.parseInt(numbers[1]); 
                int z = Integer.parseInt(numbers[2]); 
                Node currNode = new Node(x, y, z);
                if(!currNode.inBoundary(bX, bY, bZ)){
                    continue;
                }
                ArrayList<Node> children = new ArrayList<Node>();
                for(int j = 3; j < numbers.length; j++){
                    int action = Integer.parseInt(numbers[j]);
                    Node child = getChild(x, y, z, action);
                    if(child.inBoundary(bX, bY, bZ)){
                        children.add(child);
                    }
                }
                map.put(currNode, children);
            }else{
                System.out.println("File Error: wrong number of grids in the maze where there are actions available");
            }
            i++;
        }

        scanner.close();
    }
    public static Node getChild(int x, int y, int z, int action){
        switch(action){
            case 1:
            return new Node(x+1, y, z);
            
            case 2:
            return new Node(x-1, y, z);

            case 3:
            return new Node(x, y+1, z);

            case 4:
            return new Node(x, y-1, z);

            case 5:
            return new Node(x, y, z+1);

            case 6:
            return new Node(x, y, z-1);

            case 7:
            return new Node(x+1, y+1, z);

            case 8:
            return new Node(x+1, y-1, z);

            case 9:
            return new Node(x-1, y+1, z);

            case 10:
            return new Node(x-1, y-1, z);

            case 11:
            return new Node(x+1, y, z+1);

            case 12:
            return new Node(x+1, y, z-1);

            case 13:
            return new Node(x-1, y, z+1);

            case 14:
            return new Node(x-1, y, z-1);

            case 15:
            return new Node(x, y+1, z+1);

            case 16:
            return new Node(x, y+1, z-1);

            case 17:
            return new Node(x, y-1, z+1);

            case 18:
            return new Node(x, y-1, z-1);
            
        }
        return new Node(-1, -1, -1);
    }
}
