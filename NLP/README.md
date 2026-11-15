# Natural Language Processing - notebooks

Open each notebook in Google Colab (File -> Upload notebook) and run all cells. Use a GPU for the last two
(Runtime -> Change runtime type -> T4 GPU).

| Notebook | Dataset | Testing added |
|----------|---------|---------------|
| text_preprocessing_and_frequent_terms | AG News | 12 unit tests, run on unseen test articles, per-category terms, vocabulary reduction |
| movie_review_classification_bag_of_words | IMDb | Naive Bayes + SVM comparison, 5-fold cross-validation, ROC curves, custom reviews, error analysis, negation test |
| tweet_sentiment_analysis_tfidf | Sentiment140 | cleaning unit tests, 3-model comparison, ROC, accuracy by confidence, custom tweets, error analysis |
| named_entity_recognition_spacy | spaCy + own medical sentences | precision / recall / F1 on hand-labelled data, held-out medical test set, EntityRuler comparison, unseen words |
| topic_modeling_lda | 20 Newsgroups | coherence for several K, perplexity on unseen posts, topics of new documents, topics vs real groups |
| text_generation_rnn_lstm_gru | Shakespeare | held-out test loss / perplexity / accuracy, unigram baseline, next-character top-3, generated-text quality |
| news_summarization_transformer | CNN/DailyMail | ROUGE of Lead-3, extractive, T5 and fine-tuned T5, per-article scores, best/worst cases, custom article |

## Things that were corrected while adding the tests

* **Named entity recognition:** the character positions in the original custom training data were off by one, so spaCy
  ignored those entities (that is why the custom model printed nothing). Positions are now found automatically, the
  training set is larger, and a separate held-out test set is used.
* **Text generation:** the original took only the first 30,000 overlapping windows, i.e. just the first ~30,000
  characters of the play, which is why validation loss rose after epoch 1. Sequences now cover the whole text and the
  data is split into train / validation / test by position.
* **Summarization:** the ROUGE printed during training used the Trainer's short default generation, so the final
  comparison generates all summaries with the same settings (up to 128 tokens, 4 beams).
