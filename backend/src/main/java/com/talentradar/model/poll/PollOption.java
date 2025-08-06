package com.talentradar.model.poll;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entity representing individual options within polls. Stores poll choice
 * details including option text, vote counts, and ordering for structured poll
 * creation and result tracking.
 */
@Entity
@Table(name = "poll_options")
public class PollOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "poll_id", nullable = false)
    @NotNull(message = "Poll is required")
    private Poll poll;

    @Column(name = "option_text", nullable = false, length = 200)
    @NotBlank(message = "Option text is required")
    @Size(max = 200, message = "Option text cannot exceed 200 characters")
    private String optionText;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "vote_count")
    private Integer voteCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "pollOption", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PollVote> votes = new HashSet<>();

    public PollOption() {
        this.displayOrder = 0;
    }

    public PollOption(Poll poll, String optionText) {
        this.displayOrder = 0;
        this.poll = poll;
        this.optionText = optionText;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void incrementVoteCount() {
        this.voteCount++;
    }

    public void decrementVoteCount() {
        if (this.voteCount > 0) {
            this.voteCount--;
        }
    }

    public double getVotePercentage() {
        if (poll.getTotalVotes() == 0) {
            return 0.0;
        }
        return (double) voteCount / poll.getTotalVotes() * 100;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<PollVote> getVotes() {
        return votes;
    }

    public void setVotest(Set<PollVote> votes) {
        this.votes = votes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PollOption that = (PollOption) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PollOption{"
                + "id=" + id
                + ", optionText='" + optionText + '\''
                + ", voteCount=" + voteCount
                + '}';
    }
}
