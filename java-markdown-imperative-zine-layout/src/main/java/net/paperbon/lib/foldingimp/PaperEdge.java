package net.paperbon.lib.foldingimp;
  
public class PaperEdge extends PageEdge{
  private PageInfo page;
  private Imposition.Edge edge;
  
  public PaperEdge(PageInfo page, Imposition.Edge edge){
    this.edge = edge;
    setSide(page, Imposition.oppositeEdge(edge));
  }

  public Imposition.Edge getEdge(){
    return edge;
  }

  public PageInfo getPage(){
    return page;
  }
  
  @Override
  public void extendEdge(){
    
    // page <-> edge
    //  becomes
    // page <-> newCrease <-> newPage <-> edge
    
    PageInfo newPage = page.clone();
    if (edge == Imposition.Edge.RIGHT || edge == Imposition.Edge.LEFT){
      VerticalCrease newCrease = new VerticalCrease();
      if (edge == Imposition.Edge.RIGHT){
        page.right = newCrease;
        newCrease.setLeft(page);
        newCrease.setRight(newPage);
        //newCrease.left = page;
        //newCrease.right = newPage;
        newPage.left = newCrease;
        newPage.right = this;
        this.setLeft(newPage);
        //page = newPage;
      } else {//LEFT
        page.left = newCrease;
        newCrease.setRight(page);
        newCrease.setLeft(newPage);
        //newCrease.right = page;
        //newCrease.left = newPage;
        newPage.right = newCrease;
        newPage.left = this;
        this.setRight(newPage);
        //this.page = newPage;
      }
    } else {
      HorizontalCrease newCrease = new HorizontalCrease();
      if (edge == Imposition.Edge.BOTTOM){
        page.below = newCrease;
        newCrease.setAbove(page);
        newCrease.setBelow(newPage);
        //newCrease.above = page;
        //newCrease.below = newPage;
        newPage.above = newCrease;
        newPage.below = this;
        this.setAbove(newPage);
        //this.page = newPage;
      } else {//TOP
        page.above = newCrease;
        newCrease.setBelow(page);
        newCrease.setAbove(newPage);
        //newCrease.below = page;
        //newCrease.above = newPage;
        newPage.below = newCrease;
        newPage.above = this;
        this.setBelow(newPage);
        //this.page = newPage;
      }
    }
  }

  private void setSide(PageInfo page, Imposition.Edge side){
    if (side != Imposition.oppositeEdge(this.edge)){
      throw new Error();//InvalidArgumentException();
    } else {
      this.page = page;
      switch (edge){
        case LEFT:
          super.setRight(page);
          break;
        case RIGHT:
          super.setLeft(page);
          break;
        case TOP:
          super.setBelow(page);
          break;
        case BOTTOM:
        default:
          super.setAbove(page);
          break;
      }
    }
  }

  @Override
  protected void setLeft(PageInfo left){
    setSide(left, Imposition.Edge.LEFT);
  }

  @Override
  protected void setRight(PageInfo right){
    setSide(right, Imposition.Edge.RIGHT);
  }

  @Override 
  protected void setAbove(PageInfo above){
    setSide(above, Imposition.Edge.TOP);
  }

  @Override
  protected void setBelow(PageInfo below){
    setSide(below, Imposition.Edge.BOTTOM);
  }
}
