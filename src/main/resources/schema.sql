DROP TABLE IF EXISTS post_tags;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS posts;

CREATE TABLE posts (
    id SERIAL PRIMARY KEY,
    title TEXT,
    preview TEXT,
    image_url TEXT,
    text TEXT,
    likes INT DEFAULT 0
);

-- CREATE TABLE tags (
--     id SERIAL PRIMARY KEY,
--     name TEXT UNIQUE NOT NULL
-- );

CREATE TABLE post_tags (
    post_id INT REFERENCES posts(id) ON DELETE CASCADE,
    tag_id INT REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);

CREATE TABLE comments (
    id SERIAL PRIMARY KEY,
    post_id INT REFERENCES posts(id) ON DELETE CASCADE,
    author TEXT,
    content TEXT,
    created_at TIMESTAMP DEFAULT now()
);