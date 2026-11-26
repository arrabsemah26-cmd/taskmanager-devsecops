package com.example.taskmanager.service;

import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

    @Test
    void findAll_returnsAllTasks() {
        when(repository.findAll()).thenReturn(List.of(new Task("A", "a"), new Task("B", "b")));

        List<Task> result = service.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findById_returnsTask_whenExists() {
        Task task = new Task("A", "a");
        when(repository.findById(1L)).thenReturn(Optional.of(task));

        assertEquals("A", service.findById(1L).getTitle());
    }

    @Test
    void findById_throws_whenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void create_resetsIdAndSaves() {
        Task input = new Task("New", "desc");
        input.setId(42L);
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task saved = service.create(input);

        assertNull(saved.getId());
        assertEquals(TaskStatus.TODO, saved.getStatus());
        verify(repository).save(input);
    }

    @Test
    void update_changesFields() {
        Task existing = new Task("Old", "old");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task updated = new Task("New", "new");
        updated.setStatus(TaskStatus.DONE);

        Task result = service.update(1L, updated);

        assertEquals("New", result.getTitle());
        assertEquals("new", result.getDescription());
        assertEquals(TaskStatus.DONE, result.getStatus());
    }

    @Test
    void delete_removesTask() {
        Task existing = new Task("A", "a");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(repository).delete(existing);
    }

    @Test
    void delete_throws_whenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> service.delete(99L));
        verify(repository, never()).delete(any());
    }
}
