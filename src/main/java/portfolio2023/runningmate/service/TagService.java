package portfolio2023.runningmate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import portfolio2023.runningmate.domain.Tag;
import portfolio2023.runningmate.domain.dto.TagForm;
import portfolio2023.runningmate.repository.TagRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<String> findAllTagTitles(){
        return tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
    }

    public Tag findOrCreateNew(String tagTitle) {
        Tag tag = tagRepository.findByTitle(tagTitle);
        if (tag == null){
            tag = tagRepository.save(Tag.builder().title(tagTitle).build());
        }
        return tag;
    }

    public Tag findByTitle(TagForm tagForm) {
        return tagRepository.findByTitle(tagForm.getTagTitle());
    }
}
