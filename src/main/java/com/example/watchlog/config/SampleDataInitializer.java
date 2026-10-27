package com.example.watchlog.config;

import com.example.watchlog.domain.AvailabilityType;
import com.example.watchlog.domain.StreamingService;
import com.example.watchlog.domain.Work;
import com.example.watchlog.domain.WorkStreamingAvailability;
import com.example.watchlog.domain.WorkType;
import com.example.watchlog.repository.StreamingServiceRepository;
import com.example.watchlog.repository.WorkRepository;
import com.example.watchlog.repository.WorkStreamingAvailabilityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleDataInitializer {
    @Bean
    CommandLineRunner sampleData(WorkRepository workRepository,
                                 StreamingServiceRepository serviceRepository,
                                 WorkStreamingAvailabilityRepository availabilityRepository) {
        return args -> {
            if (workRepository.count() > 0 || serviceRepository.count() > 0) return;

            StreamingService netflix = serviceRepository.save(new StreamingService(
                    "Netflix", "https://www.netflix.com/jp/", null));
            StreamingService unext = serviceRepository.save(new StreamingService(
                    "U-NEXT", "https://video.unext.jp/", null));
            StreamingService prime = serviceRepository.save(new StreamingService(
                    "Prime Video", "https://www.amazon.co.jp/primevideo", null));

            Work voyage = workRepository.save(new Work(
                    "星を渡る旅人", "失われた故郷を探して、若き航海士が未知の惑星をめぐる物語。",
                    WorkType.MOVIE, 2025, "SF・アドベンチャー", "山田 太郎", null));
            Work room = workRepository.save(new Work(
                    "雨音の部屋", "古いアパートに集う住人たちの秘密を描く、静かな群像ミステリー。",
                    WorkType.DRAMA, 2024, "ミステリー", "佐藤 花子", null));
            Work kitchen = workRepository.save(new Work(
                    "深夜0時のキッチン", "閉店後の食堂を舞台に、料理が人の心をつないでいく連続ドラマ。",
                    WorkType.DRAMA, 2026, "ヒューマン", null, null));

            availabilityRepository.save(new WorkStreamingAvailability(
                    voyage, netflix, "https://www.netflix.com/jp/", AvailabilityType.SUBSCRIPTION));
            availabilityRepository.save(new WorkStreamingAvailability(
                    voyage, prime, "https://www.amazon.co.jp/primevideo", AvailabilityType.RENTAL));
            availabilityRepository.save(new WorkStreamingAvailability(
                    room, unext, "https://video.unext.jp/", AvailabilityType.SUBSCRIPTION));
            availabilityRepository.save(new WorkStreamingAvailability(
                    kitchen, netflix, "https://www.netflix.com/jp/", AvailabilityType.SUBSCRIPTION));
        };
    }
}
