CREATE TABLE IF NOT EXISTS material (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    material_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS material_version (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    material_id UUID NOT NULL REFERENCES material(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    published_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_version_status CHECK (status IN ('DRAFT', 'LIVE', 'ARCHIVED')),
    CONSTRAINT uq_material_version_number UNIQUE (material_id, version_number)
);

-- CRITICAL: Partial unique index — PostgreSQL enforces at most one LIVE version per material
CREATE UNIQUE INDEX IF NOT EXISTS idx_one_live_version ON material_version(material_id) WHERE status = 'LIVE';

CREATE INDEX IF NOT EXISTS idx_mv_material_status ON material_version(material_id, status);
