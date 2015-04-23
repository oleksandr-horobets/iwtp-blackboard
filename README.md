IWTP BlackBoard
===============

Overview
--------
IWTP-BlackBoard is an extra simple and lightweight mini-framework for decoupling application modules. Basically it is 
extended implementation of Observer Design Pattern with ability to work with different types of objects. It could
be used if your application has complex chain of processing.

Example
-------
Consider application that:

 - parses binary file
 - converts parsed data to custom business model
 - saves business object as xml document
 - saves business object as json document

For each step there are separate class:

    public class BinaryFileParser {
        ...
    
        private BlackBoard blackBoard
    
        public BinaryFileParser(BlackBoard blackBoard) {
            this.blackBoard = blackBoard;
        }
    
        public void parse(File file) {
            Map<String, String> map = new HashMap<>();
            ...
            //parsing input file to map
            ...
        
            this.blackBoard.publish(new BinaryFileParseResult(map));
        }
    }
    
    public class ParseResultConverter implements BlackBoardSubscriber<BinaryFileParseResult> {
        ...
        
        public void receive(BinaryFileParseResult parseResult) {
            BusinessObject businessObject;
            ...
            //conversion logic
            ...
            
            this.blackBoard.asyncPublish(businessObject);
        }
    }
    
    public class XmlPublisher implements BlackBoardSubscriber<BusinessObject> {
        ...
        
        public void receive(BusinessObject businessObject) {
            ...
            //saving as xml
            ...
        }
    }
    
    public class JsonPublisher implements BlackBoardSubscriber<BusinessObject> {
        ...
        
        public void receive(BusinessObject businessObject) {
            ...
            //saving as json
            ...
        }
    }
    
There are some domain objects:

    public class BinaryFileParseResult {
        private Map<String,String> map;
        
        public BinaryFileParseResult(Map<String,String> map){
            this.map = map;
        }
        
        public Map<String,String> getMap(){
            return map;
        }
    }
    
    public class BusinessObject {
        ...
        //some domain fields
        ...
    }
    
And Launcher:
    
    public class Launcher {
        public static void main(String... args) {
            BlackBoard blackBoard = new BlackBoard();
            
            blackBoard.subscribe(new ParseResultConverter());
            blackBoard.subscribe(new XmlPublisher());
            blackBoard.subscribe(new JsonPublisher());
            
            new BinaryFileParser(blackBoard).parse(new File("some-file.bin");
        }   
    }
    
Features
--------

- auto-detection of subscriber type (with some limitations)
- async-publish