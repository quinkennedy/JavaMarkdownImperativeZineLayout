package net.paperbon.lib.foldingimp;

public class VerticalCrease extends PageEdge{

  @Override
  public void extendEdge(){
    
    // start by extending the left page
    
    //create new intermediary page and crease
    VerticalCrease newCrease = new VerticalCrease();
    PageInfo newPage = this.getLeft().clone();
    //bind the right edge of the existing page, 
    // and the left edge of the new crease
    this.getLeft().right = newCrease;
    newCrease.setLeft(this.getLeft());
    //newCrease.left = left;

    //bind the left edge of the new page,
    // and the right edge of the new crease
    newPage.left = newCrease;
    newCrease.setRight(newPage);
    //newCrease.right = newPage;
    
    //bind the right edge of the new page,
    // and the left edge of THIS crease
    newPage.right = this;
    this.setLeft(newPage);
    //this.left = newPage;
    
    //do the same on the right
    
    //create new intermediary page and crease
    newCrease = new VerticalCrease();
    newPage = this.getRight().clone();
    //bind the left edge of the existing page, 
    // and the right edge of the new crease
    this.getRight().left = newCrease;
    newCrease.setRight(this.getRight());
    //newCrease.right = right;
    
    //bind the right edge of the new page,
    // and the left edge of the new crease
    newPage.right = newCrease;
    newCrease.setLeft(newPage);
    //newCrease.left = newPage;
    
    //bind the left edge of the new page,
    // and the right edge of THIS crease
    newPage.left = this;
    this.setRight(newPage);
    //this.right = newPage;
  }

  @Override
  protected void setAbove(PageInfo above){
    throw new Error();
  }

  @Override
  protected void setBelow(PageInfo below){
    throw new Error();
  }
}
