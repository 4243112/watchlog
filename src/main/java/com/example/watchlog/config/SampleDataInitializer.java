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
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SampleDataInitializer {
    @Bean
    CommandLineRunner sampleData(WorkRepository workRepository,
                                 StreamingServiceRepository serviceRepository,
                                 WorkStreamingAvailabilityRepository availabilityRepository,
                                 JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("ALTER TABLE works ALTER COLUMN type VARCHAR(16)");

            StreamingService netflix = ensureService(serviceRepository,
                    "Netflix", "https://www.netflix.com/jp/");
            StreamingService unext = ensureService(serviceRepository,
                    "U-NEXT", "https://video.unext.jp/");
            StreamingService prime = ensureService(serviceRepository,
                    "Prime Video", "https://www.amazon.co.jp/primevideo");

            Work voyage = ensureWork(workRepository, new Work(
                    "星を渡る旅人", "失われた故郷を探して、若き航海士が未知の惑星をめぐる物語。",
                    WorkType.MOVIE, 2025, "SF・アドベンチャー", "山田 太郎", null));
            Work room = ensureWork(workRepository, new Work(
                    "雨音の部屋", "古いアパートに集う住人たちの秘密を描く、静かな群像ミステリー。",
                    WorkType.DRAMA, 2024, "ミステリー", "佐藤 花子", null));
            Work kitchen = ensureWork(workRepository, new Work(
                    "深夜0時のキッチン", "閉店後の食堂を舞台に、料理が人の心をつないでいく連続ドラマ。",
                    WorkType.DRAMA, 2026, "ヒューマン", null, null));
            Work skyRail = ensureWork(workRepository, new Work(
                    "空色レイルウェイ", "雲の上を走る列車に乗り込んだ少女と、不思議な車掌の冒険を描く。",
                    WorkType.ANIME, 2025, "アニメ・ファンタジー", "高橋 凛", null));
            Work garden = ensureWork(workRepository, new Work(
                    "夏庭のロボット", "祖父の工房で目覚めた小さなロボットと少年の、ひと夏の友情物語。",
                    WorkType.ANIME, 2024, "アニメ・ファミリー", "森川 遥", null));
            Work signal = ensureWork(workRepository, new Work(
                    "月面シグナル", "月面都市から届いた謎の信号を追う科学者たちのSFサスペンス。",
                    WorkType.MOVIE, 2026, "SF・サスペンス", "伊藤 拓海", null));
            Work letters = ensureWork(workRepository, new Work(
                    "海辺の手紙", "小さな港町の郵便局を舞台に、届かなかった手紙が人々をつなぐ連続ドラマ。",
                    WorkType.DRAMA, 2023, "ヒューマン", "中村 葵", null));
            Work orbit = ensureWork(workRepository, new Work(
                    "オービット・ランナー", "宇宙コロニーを駆ける配達人が巨大な陰謀に巻き込まれていく。",
                    WorkType.ANIME, 2026, "アニメ・SF", "小林 海", null));

            ensureAvailability(availabilityRepository, voyage, netflix,
                    "https://www.netflix.com/jp/", AvailabilityType.SUBSCRIPTION);
            ensureAvailability(availabilityRepository, voyage, prime,
                    "https://www.amazon.co.jp/primevideo", AvailabilityType.RENTAL);
            ensureAvailability(availabilityRepository, room, unext,
                    "https://video.unext.jp/", AvailabilityType.SUBSCRIPTION);
            ensureAvailability(availabilityRepository, kitchen, netflix,
                    "https://www.netflix.com/jp/", AvailabilityType.SUBSCRIPTION);
            ensureAvailability(availabilityRepository, skyRail, unext,
                    "https://video.unext.jp/", AvailabilityType.SUBSCRIPTION);
            ensureAvailability(availabilityRepository, garden, prime,
                    "https://www.amazon.co.jp/primevideo", AvailabilityType.RENTAL);
            ensureAvailability(availabilityRepository, signal, prime,
                    "https://www.amazon.co.jp/primevideo", AvailabilityType.SUBSCRIPTION);
            ensureAvailability(availabilityRepository, letters, unext,
                    "https://video.unext.jp/", AvailabilityType.SUBSCRIPTION);
            ensureAvailability(availabilityRepository, orbit, netflix,
                    "https://www.netflix.com/jp/", AvailabilityType.SUBSCRIPTION);
        };
    }

    private StreamingService ensureService(StreamingServiceRepository repository, String name, String url) {
        return repository.findByNameIgnoreCase(name)
                .orElseGet(() -> repository.save(new StreamingService(name, url, null)));
    }

    private Work ensureWork(WorkRepository repository, Work candidate) {
        return repository.findFirstByTitle(candidate.getTitle())
                .orElseGet(() -> repository.save(candidate));
    }

    private void ensureAvailability(WorkStreamingAvailabilityRepository repository,
                                    Work work, StreamingService service, String viewingUrl,
                                    AvailabilityType type) {
        if (!repository.existsByWorkIdAndStreamingServiceId(work.getId(), service.getId())) {
            repository.save(new WorkStreamingAvailability(work, service, viewingUrl, type));
        }
    }
}
