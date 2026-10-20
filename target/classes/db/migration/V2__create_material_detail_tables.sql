CREATE TABLE IF NOT EXISTS pdf_version_details (
    version_id UUID PRIMARY KEY REFERENCES material_version(id) ON DELETE CASCADE,
    page_count INTEGER NOT NULL CHECK (page_count > 0),
    file_url VARCHAR(2000) NOT NULL
);

CREATE TABLE IF NOT EXISTS article_version_details (
    version_id UUID PRIMARY KEY REFERENCES material_version(id) ON DELETE CASCADE,
    word_count INTEGER NOT NULL CHECK (word_count > 0)
);

CREATE TABLE IF NOT EXISTS book_version_details (
    version_id UUID PRIMARY KEY REFERENCES material_version(id) ON DELETE CASCADE,
    author VARCHAR(500) NOT NULL
);

CREATE TABLE IF NOT EXISTS book_chapter (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version_id UUID NOT NULL REFERENCES book_version_details(version_id) ON DELETE CASCADE,
    chapter_number INTEGER NOT NULL CHECK (chapter_number > 0),
    title VARCHAR(500) NOT NULL,
    CONSTRAINT uq_book_chapter UNIQUE (version_id, chapter_number)
);

CREATE TABLE IF NOT EXISTS slide_deck_version_details (
    version_id UUID PRIMARY KEY REFERENCES material_version(id) ON DELETE CASCADE,
    slide_count INTEGER NOT NULL CHECK (slide_count > 0),
    file_url VARCHAR(2000) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pdf_page_count ON pdf_version_details(page_count);
CREATE INDEX IF NOT EXISTS idx_article_word_count ON article_version_details(word_count);
CREATE INDEX IF NOT EXISTS idx_book_chapter_version ON book_chapter(version_id);
