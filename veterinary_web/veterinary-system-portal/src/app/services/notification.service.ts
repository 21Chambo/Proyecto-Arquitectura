import { Injectable, signal } from '@angular/core';

export interface Notification {
  id: number;
  kind: 'success' | 'warning' | 'danger';
  message: string;
  title: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private nextId = 0;
  private readonly notices = signal<Notification[]>([]);
  readonly messages = this.notices.asReadonly();

  success(message: string, title: string): void { this.show('success', message, title); }
  warning(message: string, title: string): void { this.show('warning', message, title); }
  error(message: string, title: string): void { this.show('danger', message, title); }

  dismiss(id: number): void {
    this.notices.update(messages => messages.filter(message => message.id !== id));
  }

  private show(kind: Notification['kind'], message: string, title: string): void {
    const id = ++this.nextId;
    this.notices.update(messages => [...messages, { id, kind, message, title }]);
    setTimeout(() => this.dismiss(id), 5000);
  }
}
