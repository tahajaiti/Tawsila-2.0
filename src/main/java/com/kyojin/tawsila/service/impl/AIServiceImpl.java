package com.kyojin.tawsila.service.impl;

import com.kyojin.tawsila.service.AIService;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AIServiceImpl implements AIService {

    private final AzureOpenAiChatModel chatModel;

    @Autowired
    public AIServiceImpl(AzureOpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String generateResponse(String prompt) {
        try {
            var options = ChatOptions.builder().temperature(1.0).build();

            var chatPrompt = Prompt.builder().content(prompt).build();


            var response = chatModel.call(chatPrompt);
            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            return "Error generating response: " + e.getMessage();
        }
    }
}
