# Butler: CoreNlpServer

This is the server used by the Android app to parse input text from the user.

## Features

- Fast parsing of text using [StanfordCoreNLP](http://nlp.stanford.edu/software/corenlp.shtml) and [StanfordTregex](http://nlp.stanford.edu/software/tregex.shtml)
- HTTP Caching

## Routes

- `POST /semgraph`
    - Parameters:
        - `text` The text to parse. Can be a single sentence or a paragraph with multiple ones.
    - Responds with a JSON array of string representations of the SemanticGraph of each sentence in `text` and the 
    words' lemmas.
    
- `POST /sentences`
    - Parameters:
        - `text` The text to parse. Can be a single sentence or a paragraph with multiple ones.
        - `limit` Maximum number of sentences to return.
    - Responds with an array of `limit` sentences of the `text`.
    
- `POST /sentiment`
    - Parameters:
        - `text` The text to parse. Can be a single sentence or a paragraph with multiple ones.
    - Responds with an array of the sentiments of all the sentences in `text`.
    
- `POST /gender`
    - Parameters:
        - `text` The text to parse. Can be a single sentence or a paragraph with multiple ones.
    - Responds with an array of arrays. Each gender tag corresponds to a word.
    
## Running the application locally

First build with:

    $ mvn clean install

Then run it with:

    $ java -cp target/classes:target/dependency/* com.aluxian.butler.corenlpserver.Main <port>

Or, use the `start.sh` and `stop.sh` scripts.
    
The server will be available at:

    localhost:<port>
    
Default port if not specified: `7331`

## Database

The server uses a PostgreSQL database to store the text it processes through the `POST /semgraph` endpoint. It's not 
mandatory to create a database for the server to function, but you can do so if you wish.

- First, make a database named `corenlp`
- Then create a user for it
    
    - Username: `corenlp`
    - Password: `corenlp`
    
- After that, create this table:

        CREATE TABLE semgraphs
        (
            date timestamp without time zone,
            text text,
            id serial NOT NULL,
            CONSTRAINT semgraphs_pkey PRIMARY KEY (id)
        )

This should be all.

## Libraries

I don't own:

- The files in `lib/`
- The libraries declared as dependencies in `pom.xml`
- Any piece of code commented with an `@source`

Everything else is my own work.

#### Please see the README of the Android app for more info.
