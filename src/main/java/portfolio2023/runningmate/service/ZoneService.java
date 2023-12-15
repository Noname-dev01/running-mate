package portfolio2023.runningmate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import portfolio2023.runningmate.domain.Zone;
import portfolio2023.runningmate.domain.dto.ZoneForm;
import portfolio2023.runningmate.repository.ZoneRepository;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;

    @PostConstruct
    public void initZoneData() throws IOException {
        if (zoneRepository.count() == 0){
            Resource resource = new ClassPathResource("zones_kr.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            List<Zone> zoneList = reader.lines().map(line -> {
                String[] split = line.split(",");
                return Zone.builder().city(split[0]).localNameOfCity(split[1]).province(split[2]).build();
            }).collect(Collectors.toList());
//            List<Zone> zoneList = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).stream()
//                    .map(line -> {
//                        String[] split = line.split(",");
//                        return Zone.builder().city(split[0]).localNameOfCity(split[1]).province(split[2]).build();
//                    }).collect(Collectors.toList());
            zoneRepository.saveAll(zoneList);
        }
    }

    public List<String> findAllZones() {
        return zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
    }

    public Zone findByCityAndProvince(ZoneForm zoneForm) {
        return zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
    }
}
