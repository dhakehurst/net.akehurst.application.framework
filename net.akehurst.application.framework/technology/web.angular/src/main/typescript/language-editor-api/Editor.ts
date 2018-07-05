  export interface Editor {
  
    getText() : string;
    setText(value:string);
  
    onChange(handler: (text: string)=>void) : void;
  
    updateParseTree(tree: SharedPackedParseTree) : void;
  
  }

  
  export interface SharedPackedParseTree {
  
  }