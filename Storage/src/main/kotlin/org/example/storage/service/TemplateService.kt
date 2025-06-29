package org.example.storage.service

import jakarta.annotation.PostConstruct
import org.example.storage.model.Channel
import org.example.storage.model.EventType
import org.example.storage.model.Template
import org.example.storage.repository.TemplateRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class TemplateService(private val templateRepository: TemplateRepository) {
    fun findById(id: UUID): Template? = templateRepository.findById(id).orElse(null)
    fun findAll(): List<Template> = templateRepository.findAll()
    fun findByEventType(eventType: EventType): Template? =
        templateRepository.findFirstByEventType(eventType)
    fun save(template: Template): Template = templateRepository.save(template)
    fun delete(id: UUID) = templateRepository.deleteById(id)
    fun createTemplate(template: Template): Template = templateRepository.save(template)
    fun updateTemplate(id: UUID, updated: Template): Template? {
        return templateRepository.findById(id).orElse(null)?.let {
            val newTemplate = Template(
                id = id,
                eventType = updated.eventType,
                channel = updated.channel,
                text = updated.text
            )
            templateRepository.save(newTemplate)
        }
    }

    @PostConstruct
    fun initializeDefaultTemplates() {
        if (templateRepository.count() == 0L) {
            val defaultTemplates = listOf(
                Template(
                    eventType = EventType.CALL,
                    channel = Channel.BOTH,
                    text = "📞 Через {{time}} начнётся созвон {{description}}.\nМесто: {{place}}\nСсылка: {{link}}"
                ),
                Template(
                    eventType = EventType.MR,
                    channel = Channel.BOTH,
                    text = "🔃 Новый Merge Request: {{description}}\nСсылка: {{link}}"
                ),
                Template(
                    eventType = EventType.RELEASE,
                    channel = Channel.BOTH,
                    text = "🚀 Новый релиз: {{description}}\nСсылка: {{link}}"
                )
            )

            templateRepository.saveAll(defaultTemplates)
            println("✅ Созданы шаблоны по умолчанию (${defaultTemplates.size} шт.)")
        } else {
            println("ℹ️ Шаблоны уже существуют. Пропускаем инициализацию.")
        }
    }

}