package net.paperbon.lib.foldingimp;

public class PageInfo{
  int paperIndex = 0;
  Imposition.PaperSide paperSide = Imposition.PaperSide.FRONT;
  int paperColumn = 0;
  int paperRow = 0;
  int pageNumber = 0;
  boolean upsideDown = false;
  boolean backwards = false;
  PageEdge left, right, above, below;
  
  public PageInfo(){
    left = new PaperEdge(this, Imposition.Edge.LEFT);
    right = new PaperEdge(this, Imposition.Edge.RIGHT);
    above = new PaperEdge(this, Imposition.Edge.TOP);
    below = new PaperEdge(this, Imposition.Edge.BOTTOM);
  }
  
  public PageInfo clone(){
    PageInfo clone = new PageInfo();
    clone.paperIndex = this.paperIndex;
    clone.paperSide = this.paperSide;
    clone.paperColumn = this.paperColumn;
    clone.paperRow = this.paperRow;
    clone.pageNumber = this.pageNumber;
    clone.upsideDown = this.upsideDown;
    clone.backwards = this.backwards;
    return clone;
  }
  
  public String toShortString(){
    return pageNumber+(upsideDown ? "u" : "r")+(backwards ? "b" : "r");
  }
  
  public String toString(){
    return "pp"+paperIndex+
            ":"+(paperSide == Imposition.PaperSide.FRONT ? "fnt" : "bck")+
            " "+paperColumn+"x"+paperRow+
            ":"+(upsideDown ? "ud" : "ru")+
            "/"+(backwards ? "bw" : "rw")+
            " ->"+pageNumber;
  }
  
  public PageInfo extend(Imposition.Edge edge){
    PageInfo newPage = this.clone();
    PageEdge newEdge;
    if (edge == Imposition.Edge.LEFT || edge == Imposition.Edge.RIGHT){
      newEdge = new VerticalCrease();
    } else {
      newEdge = new HorizontalCrease();
    }
    PageEdge toExtend;
    switch (edge){
      case LEFT:
        toExtend = this.left;
        toExtend.setRight(newPage);
        //if (toExtend instanceof PaperEdge){
        //  ((PaperEdge)toExtend).page = newPage;
        //} else {
        //  ((VerticalCrease)toExtend).right = newPage;
        //}
        newPage.left = toExtend;
        newPage.right = newEdge;
        newEdge.setLeft(newPage);
        newEdge.setRight(this);
        //((VerticalCrease)newEdge).left = newPage;
        //((VerticalCrease)newEdge).right = this;
        this.left = newEdge;
        break;
      case RIGHT:
        toExtend = this.right;
        toExtend.setLeft(newPage);
        //if (toExtend instanceof PaperEdge){
        //  ((PaperEdge)toExtend).page = newPage;
        //} else {
        //  ((VerticalCrease)toExtend).left = newPage;
        //}
        newPage.right = toExtend;
        newPage.left = newEdge;
        newEdge.setRight(newPage);
        newEdge.setLeft(this);
        //((VerticalCrease)newEdge).right = newPage;
        //((VerticalCrease)newEdge).left = this;
        this.right = newEdge;
        break;
      case TOP:
        toExtend = this.above;
        toExtend.setBelow(newPage);
        //if (toExtend instanceof PaperEdge){
        //  ((PaperEdge)toExtend).page = newPage;
        //} else {
        //  ((HorizontalCrease)toExtend).below = newPage;
        //}
        newPage.above = toExtend;
        newPage.below = newEdge;
        newEdge.setAbove(newPage);
        newEdge.setBelow(this);
        //((HorizontalCrease)newEdge).above = newPage;
        //((HorizontalCrease)newEdge).below = this;
        this.above = newEdge;
        break;
      case BOTTOM:
        toExtend = this.below;
        toExtend.setAbove(newPage);
        //if (toExtend instanceof PaperEdge){
        //  ((PaperEdge)toExtend).page = newPage;
        //} else {
        //  ((HorizontalCrease)toExtend).above = newPage;
        //}
        newPage.below = toExtend;
        newPage.above = newEdge;
        newEdge.setBelow(newPage);
        newEdge.setAbove(this);
        //((HorizontalCrease)newEdge).below = newPage;
        //((HorizontalCrease)newEdge).above = this;
        this.below = newEdge;
        break;
    }
    return newPage;
  }
}
