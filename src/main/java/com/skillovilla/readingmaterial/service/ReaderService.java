package com.skillovilla.readingmaterial.service;

import com.skillovilla.readingmaterial.dto.request.CreateReaderRequest;
import com.skillovilla.readingmaterial.dto.response.ReaderResponse;
import com.skillovilla.readingmaterial.entity.Reader;
import com.skillovilla.readingmaterial.exception.ResourceNotFoundException;
import com.skillovilla.readingmaterial.repository.ReaderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReaderService {

    private final ReaderRepository readerRepository;

    public ReaderService(ReaderRepository readerRepository) {
        this.readerRepository = readerRepository;
    }

    @Transactional
    public ReaderResponse createReader(CreateReaderRequest request) {
        Reader reader = new Reader();
        reader.setName(request.name());
        reader = readerRepository.save(reader);

        return new ReaderResponse(
                reader.getId(),
                reader.getName(),
                reader.getCreatedAt()
        );
    }

    public Reader getReaderEntity(UUID readerId) {
        return readerRepository.findById(readerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reader", readerId.toString()));
    }
}
