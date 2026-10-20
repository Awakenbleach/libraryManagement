CREATE TABLE IF NOT EXISTS reading_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reader_id UUID NOT NULL REFERENCES reader(id) ON DELETE CASCADE,
    material_version_id UUID NOT NULL REFERENCES material_version(id) ON DELETE CASCADE,
    position_value INTEGER NOT NULL DEFAULT 0,
    position_type VARCHAR(20) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    progress_revision BIGINT NOT NULL DEFAULT 1,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_reader_version_progress UNIQUE (reader_id, material_version_id),
    CONSTRAINT chk_position_type CHECK (position_type IN ('PAGE', 'PERCENTAGE', 'CHAPTER', 'SLIDE')),
    CONSTRAINT chk_position_value CHECK (position_value >= 0)
);

CREATE INDEX IF NOT EXISTS idx_rp_reader ON reading_progress(reader_id);
CREATE INDEX IF NOT EXISTS idx_rp_version ON reading_progress(material_version_id);
CREATE INDEX IF NOT EXISTS idx_rp_reader_version ON reading_progress(reader_id, material_version_id);
