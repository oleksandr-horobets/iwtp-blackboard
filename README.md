IWTP BlackBoard
===============

Overview
--------
IWTP-BlackBoard is an extra simple and lightweight mini-framework for decoupling application modules. Basically it is 
extended implementation of Observer Design Pattern with ability to work with different types of objects. It could
be used if your application has complex chain of processing.

Features
--------

- multiple type subscriptions
- auto-detection of subscriber type (generic and anonymous classes)
- async-publish (each subscriber will use new thread from cached thread pool)

Example
-------
Consider application that:

 - parses binary file to business object
 - saves business object as xml document
 - saves business object as json document

For each step there are separate class:

    public class BinaryFileParser {
        ...
        
        public BinaryFileParser(BlackBoard blackBoard) {
            this.blackBoard = blackBoard;
        }
    
        public void parse(File file) {
            BusinessObject businessObject;
            ...
            //parsing input file to business object
            ...
        
            //let's save parse result asynchronously 
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
    
There are some domain object:
    
    public class BusinessObject {
        ...
        //some domain fields
        ...
    }
    
And Launcher:
    
    public class Launcher {
        public static void main(String... args) {
            BlackBoard blackBoard = new BlackBoard();
            
            blackBoard.subscribe(new XmlPublisher());
            blackBoard.subscribe(new JsonPublisher());
            
            new BinaryFileParser(blackBoard).parse(new File("some-file.bin");
        }   
    }
    


TO-DO
-----

- annotation subscription
- auto detection type for lambdas