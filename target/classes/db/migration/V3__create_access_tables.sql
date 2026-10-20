CREATE TABLE IF NOT EXISTS collection (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS collection_material (
    collection_id UUID NOT NULL REFERENCES collection(id) ON DELETE CASCADE,
    material_id UUID NOT NULL REFERENCES material(id) ON DELETE CASCADE,
    PRIMARY KEY (collection_id, material_id)
);

CREATE TABLE IF NOT EXISTS reader (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS reader_collection_access (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reader_id UUID NOT NULL REFERENCES reader(id) ON DELETE CASCADE,
    collection_id UUID NOT NULL REFERENCES collection(id) ON DELETE CASCADE,
    access_end_date DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_reader_collection UNIQUE (reader_id, collection_id)
);

CREATE TABLE IF NOT EXISTS reader_material_access (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reader_id UUID NOT NULL REFERENCES reader(id) ON DELETE CASCADE,
    material_id UUID NOT NULL REFERENCES material(id) ON DELETE CASCADE,
    access_end_date DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_reader_material UNIQUE (reader_id, material_id)
);

CREATE INDEX IF NOT EXISTS idx_cm_collection ON collection_material(collection_id);
CREATE INDEX IF NOT EXISTS idx_cm_material ON collection_material(material_id);
CREATE INDEX IF NOT EXISTS idx_rca_reader ON reader_collection_access(reader_id);
CREATE INDEX IF NOT EXISTS idx_rca_collection ON reader_collection_access(collection_id);
CREATE INDEX IF NOT EXISTS idx_rma_reader ON reader_material_access(reader_id);
CREATE INDEX IF NOT EXISTS idx_rma_material ON reader_material_access(material_id);
