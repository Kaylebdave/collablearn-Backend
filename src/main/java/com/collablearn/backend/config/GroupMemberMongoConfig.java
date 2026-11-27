package com.collablearn.backend.config;

import com.collablearn.backend.model.GroupMember;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
public class GroupMemberMongoConfig {

    @Bean
    MongoCustomConversions mongoCustomConversions() {
        return MongoCustomConversions.create(adapter -> adapter.registerConverter(new LegacyMemberConverter()));
    }

    @ReadingConverter
    static class LegacyMemberConverter implements Converter<String, GroupMember> {
        @Override
        public GroupMember convert(String name) {
            return new GroupMember(null, name, "member");
        }
    }
}