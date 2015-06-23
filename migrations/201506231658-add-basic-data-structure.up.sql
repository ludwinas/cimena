-- up
CREATE TABLE movies
(id SERIAL PRIMARY KEY,
 title VARCHAR,
 original_title VARCHAR,
 link VARCHAR,
 is_watched BOOLEAN,
 date_added TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW());

CREATE TABLE movie_tag
(id SERIAL PRIMARY KEY,
 label VARCHAR NOT NULL,
 color VARCHAR);

CREATE TABLE movies_movie_tag
(movie_id INT REFERENCES movies(id) ON DELETE CASCADE,
 movie_tag_id INT REFERENCES movie_tag(id) ON DELETE CASCADE,
 UNIQUE(movie_id, movie_tag_id));
