package net.paperbon.lib.foldingimp;

public abstract class PageEdge{
  private PageInfo left = null;
  private PageInfo right = null;
  private PageInfo above = null;
  private PageInfo below = null;

  public abstract void extendEdge();

  public PageInfo getLeft(){
    return left;
  }
  public PageInfo getRight(){
    return right;
  }
  public PageInfo getAbove(){
    return above;
  }
  public PageInfo getBelow(){
    return below;
  }

  protected void setLeft(PageInfo left){
    this.left = left;
  }
  protected void setRight(PageInfo right){
    this.right = right;
  }
  protected void setAbove(PageInfo above){
    this.above = above;
  }
  protected void setBelow(PageInfo below){
    this.below = below;
  }
}
