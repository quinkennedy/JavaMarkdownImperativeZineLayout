package net.paperbon.lib.foldingimp;

import java.util.ArrayList;

public class Imposition{
  public PageInfo[] info;
  private int numRows = 1;
  private int numColumns = 1;
  private int numPapers = 1;
  private int numPages = 2;
  private static boolean DEBUG = false;
  
  private void println(String s){
    if (DEBUG){
      System.out.println(s);
    }
  }
  
  private void println(){
    if (DEBUG){
      System.out.println();
    }
  }
  
  private void print(String s){
    if (DEBUG){
      System.out.print(s);
    }
  }
  
  public int getNumPages(){
    return numPages;
  }
  
  public int getNumRows(){
    return numRows;
  }
  
  public int getNumColumns(){
    return numColumns;
  }
  
  public Imposition(int numPages){
    ArrayList<Integer> folds = new ArrayList<Integer>();
    if (numPages <= 2){
      //no folds
    } else {
      //always end with a half fold, for binding
      folds.add(2);
      if (numPages <= 4){
        //one half fold is sufficient, nothing left to do
      } else {
        calculateFolds(numPages, 4, folds);
      }
    }
    
    //apply folds in reverse order
    // currently printing out for debug
    String toPrint = numPages + ": ";
    for(int i = folds.size() - 1; i >= 0; i--){
      toPrint += folds.get(i) + ",";
    }
    println(toPrint);
    // construct the fold set
    FoldSet[] foldOrder = new FoldSet[folds.size()];
    for(int i = folds.size() - 1, k = 0; i >= 0; i--, k++){
      PaperFold[] set = new PaperFold[folds.get(i) - 1];
      for(int j = 0; j < set.length; j++){
        set[j] = new PaperFold(PaperFold.Topography.VALLEY);
      }
      foldOrder[k] = new FoldSet(set, 
                                 (i % 2 == 0 ? 
                                  FoldSet.Direction.VERTICAL : 
                                  FoldSet.Direction.HORIZONTAL),
                                 true);
    }
    init(foldOrder, 1, false);
  }
  
  private int calculateFolds(int targetPages, int currPages, ArrayList<Integer> folds){
    int currIndex = folds.size() - 1;
    //if a half fold meets the requirement, lets do that
    if (currPages * 2 >= targetPages){
      folds.add(2);
      return currPages * 2;
    //otherwise, if a third fold meets the requirement, do _that_
    } else if (currPages * 3 >= targetPages){
      folds.add(3);
      return currPages * 3;
    //otherwise, get the minimum resulting pages of applying either a half or third fold
    // and pick whichever one is smallest
    } else {
      ArrayList<Integer> twoFolds = new ArrayList<Integer>(folds.size() + 2);
      ArrayList<Integer> threeFolds = new ArrayList<Integer>(folds.size() + 2);
      for(int i = 0; i < folds.size(); i++){
        int foldCopy = folds.get(i);
        twoFolds.add(foldCopy);
        threeFolds.add(foldCopy);
      }
      twoFolds.add(2);
      threeFolds.add(3);
      int twoPages = calculateFolds(targetPages, currPages * 2, twoFolds);
      int threePages = calculateFolds(targetPages, currPages * 3, threeFolds);
      ArrayList<Integer> targetFolds;
      int targetNumPages;
      if (twoPages <= threePages){
        targetFolds = twoFolds;
        targetNumPages = twoPages;
      } else {
        targetFolds = threeFolds;
        targetNumPages = threePages;
      }
      for(int i = currIndex + 1; i < targetFolds.size(); i++){
        folds.add(targetFolds.get(i));
      }
      return targetNumPages;
    }
  }
  
  public Imposition(FoldSet[] foldOrder, int numPapers, boolean multipleSignatures){
    init(foldOrder, numPapers, multipleSignatures);
  }
  
  private void init(FoldSet[] foldOrder, int numPapers, boolean multipleSignatures){
    PageInfo[] curr = new PageInfo[]{new PageInfo(), new PageInfo()};
    curr[1].paperSide = PaperSide.BACK;
    curr[1].backwards = true;
    curr[1].pageNumber = 1;
    for(int i = 0; i < foldOrder.length; i++){
      curr = applyFolds(curr, foldOrder[i]);
    }
    updateRowsAndColumns(curr);
    info = curr;
    numPages = info.length;
    numRows = numRows(info);
    numColumns = numColumns(info);
  }
  
  private void updateColumns(PageInfo currPage, int rowNumber){
    int column = 0;
    while(true){//iterate across row
      currPage.paperRow = rowNumber;
      currPage.paperColumn = column;
      column++;
      if (currPage.right instanceof PaperEdge){
        break;
      } else {
        currPage = currPage.right.getRight();
        //currPage = ((VerticalCrease)(currPage.right)).right;
      }
    }
  }
  
  private void updateRowsAndColumns(PageInfo[] pages){
    //just start at the top page, and go left and up until we hit the top-left corner
    PageInfo currFrontRow = getTopLeft(pages[0]);
    PageInfo currBackRow = getTopLeft(pages[1]);
    if (currFrontRow.paperSide == PaperSide.BACK){
      PageInfo tmp = currBackRow;
      currBackRow = currFrontRow;
      currFrontRow = tmp;
    }
    
    //for each row (from top -> bottom) 
    //go through each page in the row (from left -> right)
    //and update the row/column indices
    int row = 0;
    while(true){//iterate through rows
      updateColumns(currFrontRow, row);
      updateColumns(currBackRow, row);
      row++;
      if (currFrontRow.below instanceof PaperEdge){
        break;
      } else {
        currFrontRow = currFrontRow.below.getBelow();
        currBackRow = currBackRow.below.getBelow();
        //currFrontRow = ((HorizontalCrease)(currFrontRow.below)).below;
        //currBackRow = ((HorizontalCrease)(currBackRow.below)).below;
      }
    }
  }
  
  private String getRowString(PageInfo currPage){
    String output = "|";
    while(true){//iterate across row
      output += currPage.toShortString() + "|";
      if (currPage.right instanceof PaperEdge){
        break;
      } else {
        currPage = currPage.right.getRight();
        //currPage = ((VerticalCrease)(currPage.right)).right;
      }
    }
    return output;
  }
  
  public String toString(PageInfo[] pages){
    PageInfo currFrontRow = getTopLeft(pages[0]);
    PageInfo currBackRow = getTopLeft(pages[1]);
    if (currFrontRow.paperSide == PaperSide.BACK){
      PageInfo tmp = currBackRow;
      currBackRow = currFrontRow;
      currFrontRow = tmp;
    }
    int numColumns = numColumns(pages);
    String output = "";
    
    //create headers for page sides
    for(int i = 0; i < Math.floor(((numColumns * 4) + 1 - 4)/2.0); i++){
      output += "_";
    }
    output += "frnt";
    for(int i = 0; i < Math.ceil(((numColumns * 4) + 1 - 4)/2.0); i++){
      output += "_";
    }
    output += "  ";
    for(int i = 0; i < Math.floor(((numColumns * 4) + 1 - 4)/2.0); i++){
      output += "_";
    }
    output += "back";
    for(int i = 0; i < Math.ceil(((numColumns * 4) + 1 - 4)/2.0); i++){
      output += "_";
    }
    output += "\n";
    
    
    while(true){//iterate through rows
      output += getRowString(currFrontRow);
      output += "  ";
      output += getRowString(currBackRow);
      output += "\n";
      
      //insert row separators
      for(int i = 0; i < numColumns * 4 + 1; i++){
        output += "-";
      }
      output += "  ";
      for(int i = 0; i < numColumns * 4 + 1; i++){
        output += "-";
      }
      output += "\n";
      
      //go to next row
      if (currFrontRow.below instanceof PaperEdge){
        break;
      } else {
        currFrontRow = currFrontRow.below.getBelow();
        currBackRow = currBackRow.below.getBelow();
        //currFrontRow = ((HorizontalCrease)(currFrontRow.below)).below;
        //currBackRow = ((HorizontalCrease)(currBackRow.below)).below;
      }
    }
    
    return output;
  }
  
  private PageInfo getTopLeft(PageInfo currRow){
    while (!(currRow.left instanceof PaperEdge)){
      currRow = currRow.left.getLeft();
      //currRow = ((VerticalCrease)(currRow.left)).left;
    }
    while (!(currRow.above instanceof PaperEdge)){
      currRow = currRow.above.getAbove();
      //currRow = ((HorizontalCrease)(currRow.above)).above;
    }
    return currRow;
  }
  
  private PageEdge[] getEdges(PageInfo[] pages, Edge edge){
    ArrayList<PageEdge> edges = new ArrayList<PageEdge>();
    for(int i = 0; i < pages.length; i++){
      PageInfo currPage = pages[i];
      PageEdge currEdge;
      switch (edge){
        case LEFT:
          currEdge = currPage.backwards ? currPage.right : currPage.left;
          break;
        case RIGHT:
          currEdge = currPage.backwards ? currPage.left : currPage.right;
          break;
        case BOTTOM:
          currEdge = currPage.backwards ? currPage.above : currPage.below;
          break;
        case TOP:
        default:
          currEdge = currPage.backwards ? currPage.below : currPage.above;
          break;
      }
      if (currEdge instanceof PaperEdge){
        edges.add(currEdge);
      } else {
        if ((edge == Edge.LEFT || edge == Edge.RIGHT) && !currPage.backwards){
          edges.add(currEdge);
        } else if ((edge == Edge.TOP || edge == Edge.BOTTOM) && !currPage.upsideDown){
          edges.add(currEdge);
        }
      }
    }
    return edges.toArray(new PageEdge[edges.size()]);
  }
  
  private PageInfo[] applyFolds(PageInfo[] start, FoldSet folds){
    println("[applyFolds] start:" + start.length + " folds:" + folds.set.length);
    println(this.toString(start));
    PageInfo[] curr = start;
    Edge edge = (folds.direction == FoldSet.Direction.HORIZONTAL ?
                 (folds.fromTopOrLeft ? Edge.BOTTOM : Edge.TOP) :
                 (folds.fromTopOrLeft ? Edge.RIGHT : Edge.LEFT));
    //PageEdge[] edges = getEdges(start, edge);
    
    for(int i = 0; i < folds.set.length; i++){
      curr = applyFold(start, curr, folds.set[i], edge);
      println("[applyFolds] pages");
      println(this.toString(curr));
      println("[applyFolds] array");
      for(int j = 0; j < curr.length; j++){
        print(" | " + curr[j].toShortString());
      }
      println();
    }
    println("[applyFolds] end:" + curr.length);
    return curr;
  }
  
  private int numRows(PageInfo[] pages){
    int numRows = 1;
    PageInfo currPage = pages[0];
    while (!(currPage.above instanceof PaperEdge)){
      currPage = currPage.above.getAbove();
      //currPage = ((HorizontalCrease)(currPage.above)).above;
      numRows++;
    }
    currPage = pages[0];
    while (!(currPage.below instanceof PaperEdge)){
      currPage = currPage.below.getBelow();
      //currPage = ((HorizontalCrease)(currPage.below)).below;
      numRows++;
    }
    return numRows;
  }
  
  private int numColumns(PageInfo[] pages){
    int numColumns = 1;
    PageInfo currPage = pages[0];
    while (!(currPage.left instanceof PaperEdge)){
      currPage = currPage.left.getLeft();
      //currPage = ((VerticalCrease)(currPage.left)).left;
      numColumns++;
    }
    currPage = pages[0];
    while (!(currPage.right instanceof PaperEdge)){
      currPage = currPage.right.getRight();
      //currPage = ((VerticalCrease)(currPage.right)).right;
      numColumns++;
    }
    return numColumns;
  }
  
  private void connectNeighbor(PageInfo page, Edge edgeToGo){
    //go to the pages connected neighbor
    PageInfo connectedNeighbor;
    switch (edgeToGo){
      case RIGHT:
        connectedNeighbor = page.right.getRight();
        //connectedNeighbor = ((VerticalCrease)(page.right)).right;
        break;
      case LEFT:
        connectedNeighbor = page.left.getLeft();
        //connectedNeighbor = ((VerticalCrease)(page.left)).left;
        break;
      case TOP:
        connectedNeighbor = page.above.getAbove();
        //connectedNeighbor = ((HorizontalCrease)(page.above)).above;
        break;
      case BOTTOM:
      default:
        connectedNeighbor = page.below.getBelow();
        //connectedNeighbor = ((HorizontalCrease)(page.below)).below;
        break;
    }
    
    // if we moved vertically, then get that neighbor's right edge
    PageEdge neighborEdge;
    if (edgeToGo == Edge.TOP || edgeToGo == Edge.BOTTOM){
      neighborEdge = connectedNeighbor.right;
    }
    // if we moved horizontally, then get that neighbor's bottom edge
    else {
      neighborEdge = connectedNeighbor.below;
    }
    
    //if the edge is a PaperEdge, then there is no unconnected neigbbor
    if (!(neighborEdge instanceof PaperEdge)){
      //get the edge's other neighbor
      PageInfo diagonalNeighbor;
      if (edgeToGo == Edge.TOP || edgeToGo == Edge.BOTTOM){
        diagonalNeighbor = neighborEdge.getRight();
        //diagonalNeighbor = ((VerticalCrease)neighborEdge).right;
      }
      // if we moved horizontally, then go to that neighbor's bottom neighbor
      else {
        diagonalNeighbor = neighborEdge.getBelow();
        //diagonalNeighbor = ((HorizontalCrease)neighborEdge).below;
      }
      
      //now transition to the unconnected neighbor
      PageInfo disconnectedNeighbor;
      switch(edgeToGo){
        case TOP:
          disconnectedNeighbor = diagonalNeighbor.below.getBelow();
          //disconnectedNeighbor = ((HorizontalCrease)(diagonalNeighbor.below)).below;
          break;
        case BOTTOM:
          disconnectedNeighbor = diagonalNeighbor.above.getAbove();
          //disconnectedNeighbor = ((HorizontalCrease)(diagonalNeighbor.above)).above;
          break;
        case RIGHT:
          disconnectedNeighbor = diagonalNeighbor.left.getLeft();
          //disconnectedNeighbor = ((VerticalCrease)(diagonalNeighbor.left)).left;
          break;
        case LEFT:
        default:
          disconnectedNeighbor = diagonalNeighbor.right.getRight();
          //disconnectedNeighbor = ((VerticalCrease)(diagonalNeighbor.right)).right;
          break;
      }
      
      //and finally connect the disconnected neighbors
      if (edgeToGo == Edge.TOP || edgeToGo == Edge.BOTTOM){
        VerticalCrease newCrease = new VerticalCrease();
        page.right = (PageEdge)newCrease;
        newCrease.setLeft(page);
        newCrease.setRight(disconnectedNeighbor);
        //newCrease.left = page;
        //newCrease.right = disconnectedNeighbor;
        disconnectedNeighbor.left = (PageEdge)newCrease;
      } else {
        HorizontalCrease newCrease = new HorizontalCrease();
        page.below = (PageEdge)newCrease;
        newCrease.setAbove(page);
        newCrease.setBelow(disconnectedNeighbor);
        //newCrease.above = page;
        //newCrease.below = disconnectedNeighbor;
        disconnectedNeighbor.above = (PageEdge)newCrease;
      }
    }
  }
  
  private void extendEdges(PageEdge[] edges){
    //first extend all the edges
    for(int i = 0; i < edges.length; i++){
      edges[i].extendEdge();
    }
    //for each edge
    for(int i = 0; i < edges.length; i++){
      //if it is a paper edge
      if (edges[i] instanceof PaperEdge){
        //we only need to connect one extended page
        connectNeighbor(((PaperEdge)(edges[i])).getPage(), 
                        oppositeEdge(((PaperEdge)(edges[i])).getEdge()));
                        //oppositeEdge(((PaperEdge)(edges[i])).edge));
      } else {
        //otherwise we need to connect both sides of the crease
        if (edges[i] instanceof VerticalCrease){
          connectNeighbor(edges[i].getLeft(), Edge.LEFT);
          connectNeighbor(edges[i].getRight(), Edge.RIGHT);
          //connectNeighbor(((VerticalCrease)(edges[i])).left, Edge.LEFT);
          //connectNeighbor(((VerticalCrease)(edges[i])).right, Edge.RIGHT);
        } else {
          connectNeighbor(edges[i].getAbove(), Edge.TOP);
          connectNeighbor(edges[i].getBelow(), Edge.BOTTOM);
          //connectNeighbor(((HorizontalCrease)(edges[i])).above, Edge.TOP);
          //connectNeighbor(((HorizontalCrease)(edges[i])).below, Edge.BOTTOM);
        }
      }
    }
  }
  
  private PageInfo[] applyFold(PageInfo[] original, PageInfo[] prev, PaperFold fold, Edge edge){
    PageInfo[] next = new PageInfo[prev.length + original.length];
    println("[applyFold] start:" + original.length + " prev:" + prev.length + " next:" + next.length);
    println("[applyFold] " + edgeString(edge) + 
                       " " + (fold.topography == PaperFold.Topography.VALLEY ? 
                              "VALLEY" : 
                              "MOUNTAIN"));
    //for each page in the starting set ("original" list)
    // extend the page in the specified direction (toward the specified "edge")
    PageInfo[] extended = new PageInfo[original.length];
    for(int i = 0; i < original.length; i++){
      PageInfo toExtend = original[i];
      Edge toward;
      if (((edge == Edge.LEFT || edge == Edge.RIGHT) && 
           toExtend.backwards) ||
          ((edge == Edge.TOP || edge == Edge.BOTTOM) &&
           toExtend.upsideDown)){
        toward = oppositeEdge(edge);
      } else {
        toward = edge;
      }
      extended[i] = toExtend.extend(toward);
      println("[applyFold] new page:" + extended[i]);
    }
    //then for each newly created page, connect them to their new neighbors
    for(int i = 0; i < extended.length; i++){
      PageInfo toConnect = extended[i];
      Edge toward;
      if (((edge == Edge.LEFT || edge == Edge.RIGHT) &&
           toConnect.backwards) ||
          ((edge == Edge.TOP || edge == Edge.BOTTOM) &&
           toConnect.upsideDown)){
        toward = edge;
       } else {
         toward = oppositeEdge(edge);
       }
       connectNeighbor(toConnect, toward);
    }
    
    //now put the previous page info in the right place
    // if it is a valley fold, the previous PageInfo will end up on top
    //  otherwise it ends up underneath
    // eitherway it gets reversed
    int j;
    if (fold.topography == PaperFold.Topography.VALLEY){
      j = 0;
    } else {
      j = extended.length;
    }
    for (int i = prev.length - 1; i >= 0; i--, j++){
      PageInfo page = prev[i];//.clone()?
      page.pageNumber = j;
      if (edge == Edge.TOP || edge == Edge.BOTTOM){
        page.upsideDown = !page.upsideDown;
      } else {
        page.backwards = !page.backwards;
      }
      next[j] = page;
    }
    
    //now put the new pages in the right place
    // if it a valley fold, the new pages will end up on the bottom,
    // otherwise on top
    if (fold.topography == PaperFold.Topography.VALLEY){
      //nothing to do, index can roll over from previous
    } else {
      j = 0;
    }
    for(int i = 0; i < extended.length; i++, j++){
      next[j] = extended[i];
      next[j].pageNumber = j;
      //migrate extended to original
      // since original is passed by reference, this way we can "give back"
      // the references to the "fresh edge"
      original[i] = extended[i];
    }
    
    return next;
  }
  
  private PageInfo[] applyHalfFold(PageInfo[] prev, boolean isHorizontal, boolean isValley, boolean foldUpOrLeft){
    PageInfo[] next = new PageInfo[prev.length * 2];
    int j = 0;
    if (isValley){
      for(int i = prev.length - 1; i >= 0; i--, j++){
        next[j] = prev[i].clone();
        next[j].pageNumber = j;
        if (isHorizontal){
          next[j].upsideDown = !next[j].upsideDown;
          if (next[j].paperRow % 2 == (foldUpOrLeft ? 0 : 1)){
            next[j].paperRow = next[j].paperRow * 2 + 1;
          } else {
            next[j].paperRow = next[j].paperRow * 2;
          }
        } else {
          if (next[j].paperColumn % 2 == (foldUpOrLeft ? 0 : 1)){
            next[j].paperColumn = next[j].paperColumn * 2 + 1;
          } else {
            next[j].paperColumn = next[j].paperColumn * 2;
          }
        }
      }
    }
    for(int i = 0; i < prev.length; i++, j++){
      next[j] = prev[i].clone();
      next[j].pageNumber = j;
      if (isHorizontal){
        if (next[j].paperRow % 2 == (foldUpOrLeft ? 1 : 0)){
          next[j].paperRow = next[j].paperRow * 2 + 1;
        } else {
          next[j].paperRow = next[j].paperRow * 2;
        }
      } else {
        if (next[j].paperColumn % 2 == (foldUpOrLeft ? 1 : 0)){
          next[j].paperColumn = next[j].paperColumn * 2 + 1;
        } else {
          next[j].paperColumn = next[j].paperColumn * 2;
        }
      }
    }
    if (!isValley){
      for(int i = prev.length - 1; i >= 0; i--, j++){
        next[j] = prev[i].clone();
        next[j].pageNumber = j;
        if (isHorizontal){
          next[j].upsideDown = !next[j].upsideDown;
          if (next[j].paperRow % 2 == (foldUpOrLeft ? 0 : 1)){
            next[j].paperRow = next[j].paperRow * 2 + 1;
          } else {
            next[j].paperRow = next[j].paperRow * 2;
          }
        } else {
          if (next[j].paperColumn % 2 == (foldUpOrLeft ? 0 : 1)){
            next[j].paperColumn = next[j].paperColumn * 2 + 1;
          } else {
            next[j].paperColumn = next[j].paperColumn * 2;
          }
        }
      }
    }
    return next;
  }
  
  public PageInfo getPage(int pageNumber){
    return null;
  }
  
  public PageInfo getPage(int paperIndex, int rowNum, int columnNum){
    return null;
  }
  
  public Imposition(Style style){
  }
  
  public enum PaperSide{
    FRONT, BACK
  }
  
  public enum PageFolds{
    HorizontalHalf, VerticalHalf, HorizontalThirds, VerticalThirds
  }
  
  public enum Style{
    ClassicZine
  }
  
  public enum Edge{
    LEFT, RIGHT, TOP, BOTTOM
  }
    
  public static Edge oppositeEdge(Edge edge){
    switch (edge){
      case LEFT:
        return Edge.RIGHT;
      case RIGHT:
        return Edge.LEFT;
      case TOP:
        return Edge.BOTTOM;
      case BOTTOM:
      default:
        return Edge.TOP;
    }
  }
  
  public static String edgeString(Edge edge){
    switch (edge){
      case LEFT:
        return "LEFT";
      case RIGHT:
        return "RIGHT";
      case TOP:
        return "TOP";
      case BOTTOM:
        return "BOTTOM";
      default:
        return "default";
    }
  }
}
