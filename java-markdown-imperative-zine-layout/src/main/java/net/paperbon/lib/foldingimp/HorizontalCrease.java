package net.paperbon.lib.foldingimp;

public class HorizontalCrease extends PageEdge{
  PageInfo above, below;

  @Override
  public void extendEdge(){
    
    //start by extending the bottom page
    
    //create new intermediary page and crease
    HorizontalCrease newCrease = new HorizontalCrease();
    PageInfo newPage = this.getBelow().clone();
    //bind the top edge of the existing page, 
    // and the bottom edge of the new crease
    this.getBelow().above = newCrease;
    newCrease.setBelow(this.getBelow());
    //newCrease.below = below;

    //bind the bottom edge of the new page,
    // and the top edge of the new crease
    newPage.below = newCrease;
    newCrease.setAbove(newPage);
    //newCrease.above = newPage;

    //bind the top edge of the new page,
    // and the bottom edge of THIS crease
    newPage.above = this;
    this.setBelow(newPage);
    //this.below = newPage;
    
    //do the same on the top
    
    //create new intermediary page and crease
    newCrease = new HorizontalCrease();
    newPage = this.getAbove().clone();
    //bind the bottom edge of the existing page, 
    // and the top edge of the new crease
    this.getAbove().below = newCrease;
    newCrease.setAbove(this.getAbove());
    //newCrease.above = above;

    //bind the top edge of the new page,
    // and the bottom edge of the new crease
    newPage.above = newCrease;
    newCrease.setBelow(newPage);
    //newCrease.below = newPage;

    //bind the bottom edge of the new page,
    // and the top edge of THIS crease
    newPage.below = this;
    this.setAbove(newPage);
    //this.above = newPage;
  }

  @Override
  protected void setLeft(PageInfo left){
    throw new Error();
  }

  @Override
  protected void setRight(PageInfo right){
    throw new Error();
  }    
}
