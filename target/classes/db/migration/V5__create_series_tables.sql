CREATE TABLE IF NOT EXISTS series (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS series_part (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    series_id UUID NOT NULL REFERENCES series(id) ON DELETE CASCADE,
    material_id UUID NOT NULL REFERENCES material(id) ON DELETE CASCADE,
    part_order INTEGER NOT NULL CHECK (part_order > 0),
    CONSTRAINT uq_series_part_order UNIQUE (series_id, part_order),
    CONSTRAINT uq_series_material UNIQUE (series_id, material_id)
);

CREATE INDEX IF NOT EXISTS idx_sp_series ON series_part(series_id);
CREATE INDEX IF NOT EXISTS idx_sp_material ON series_part(material_id);
